const path = require('path');
const fs = require("fs")

const UglifyJSPlugin = require('uglifyjs-webpack-plugin')

module.exports ={
  entry: './src/index.js',
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'dist'),
    publicPath: 'http://localhost:3000/'
  },
  devtool: 'source-map',

  module: {
    rules: [
      {
        test: /\.js$/,
        loader: 'babel-loader'
      },
      {
        test: /\.svg$/,
        loader: 'babel-loader!svg-react-loader'
      },
      {
        test: /\.(less|css)$/,
        use: ['css-loader', 'less-loader']
      }
    ]
  },

  optimization: {
    minimizer: [
      new UglifyJSPlugin({
        sourceMap: true,
      })
    ]
  },

  plugins: [      function () {
    this.plugin('done', function (stats) {

      const move = (source, target) => {
        if (!fs.existsSync(source)) {
          // nothing to move
          return
        }
        // unlink old
        if (fs.existsSync(target)) {
          fs.unlinkSync(target)
        }
        fs.rename(source, target, () => {})
      }

      // if(process.env.NODE_ENV === "development"){
      //   console.log("MOVING");
      //   move(path.join(__dirname, '/dist/main.js'), path.join(__dirname, '../backend/build/resources/main/static/main.js'));
      //   move(path.join(__dirname, '/dist/main.js.map'), path.join(__dirname, '../backend/build/resources/main/static/main.js.map'));
      // }
    });
  }]
};