import React from 'react'
import Grid from '@material-ui/core/Grid'
import Paper from '@material-ui/core/Paper'
import PropTypes from 'prop-types'
import { withStyles } from '@material-ui/core/styles'
import _ from 'lodash'
import Button from '@material-ui/core/Button'
import axios from 'axios'
import Typography from '@material-ui/core/Typography'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    height: 75,
    width: 75,
    textAlign: 'center',
    fontSize: '42px',
    lineHeight: '70px'
  },
  results: {
    marginTop: 20
  },
  word: {
    fontSize: '16px',
    display: 'inline-block',
    marginRight: '10px',
    padding: '8px',
    marginBottom: '7px'
  },
  selected: {
    backgroundColor: '#AAA'
  },
  control: {
    padding: theme.spacing.unit * 2,
  },
  fab: {
    margin: theme.spacing.unit,
  },
  extendedIcon: {
    marginRight: theme.spacing.unit,
  },
  button: {
    margin: theme.spacing.unit,
  },
  buttons: {
    marginTop: '24px'
  },
  mainBody: {
    display: 'flex',
    justifyContent: 'flex-start',
    alignItems: 'stretch',
    alignContent: 'stretch',
    flexDirection: 'row'
  }
})

// Actual US Dice set
const DICE = [
  ['V', 'I', 'T', 'E', 'G', 'N'],
  ['A', 'C', 'E', 'S', 'L', 'R'],
  ['V', 'A', 'Z', 'E', 'D', 'N'],
  ['I', 'C', 'A', 'T', 'A', 'O'],
  ['N', 'O', 'D', 'U', 'K', 'T'],
  ['E', 'N', 'I', 'P', 'H', 'S'],
  ['O', 'R', 'I', 'F', 'B', 'X'],
  ['K', 'U', 'L', 'E', 'G', 'Y'],
  ['E', 'Y', 'I', 'E', 'H', 'F'],
  ['E', 'S', 'U', 'T', 'L', 'P'],
  ['E', 'W', 'O', 'S', 'D', 'N'],
  ['P', 'E', 'C', 'A', 'D', 'M'],
  ['A', 'L', 'I', 'B', 'T', 'Y'],
  ['S', 'A', 'H', 'O', 'M', 'R'],
  ['J', 'A', 'B', 'O', 'M', 'Q'], // Qu not supported yet
  ['U', 'R', 'I', 'G', 'L', 'W']
]

class Board extends React.Component {
  constructor () {
    super()
    this.state = {
      board: this.generateRandomBoard()
    }

    window.onkeypress = (event) => {
      if (this.state.selected &&
        ((event.keyCode >= 65 && event.keyCode <= 90) // upper case
          || event.keyCode >= 97 && event.keyCode <= 122)  // lower case
      ) {
        let newBoard = [...this.state.board]
        newBoard[(this.state.selected[0] * 4) + this.state.selected[1]] = event.key.toUpperCase()
        this.setState({board: newBoard})
      }
    }
  }

  generateRandomBoard () {
    const selectedDie = new Set()
    while (selectedDie.size < 16) {
      let die = Math.round(Math.random() * (DICE.length - 1))
      if (!selectedDie.has(die)) {
        selectedDie.add(die)
      }
    }
    console.log(selectedDie)

    const faces = []
    Array.from(selectedDie).forEach((die) =>
      faces.push(DICE[die][Math.round(Math.random() * 5)])
    )
    console.log(faces)
    return faces

  }

  randomize () {
    this.setState({board: this.generateRandomBoard()})
  }

  solve () {
    axios({
      method: 'POST',
      url: 'api/v1/boggle/solve',
      headers: {'Content-type': 'application/json'},
      data: JSON.stringify({
        values: this.state.board.reduce((accum, val, idx) => {
          if (idx % 4 === 0) {
            accum.push([])
          }
          accum[accum.length - 1].push(val)
          return accum
        }, [])
      })
    }).then(response => {
      this.setState({
        wordsInBoard: _.get(response, 'data.entries')
      })
    }).catch(error => {
      console.error(error)
    })
  }

  solution () {
    if (!this.state.wordsInBoard) {
      return <div style={{margin: '0 50px'}}>

        <Typography variant="h3" gutterBottom>
          Options:
        </Typography>
        <ol style={{fontSize: '18px'}}>
          <li>Solve a Random board</li>
          <li>Solve a custom board by selecting individual cells and pressing a key to assign a letter</li>
        </ol>
      </div>
    }
    return <div className={this.props.classes.results}>
      <Typography variant="h3" gutterBottom align={'left'}>
        Possible Words ({this.state.wordsInBoard.length}):
      </Typography>
      {this.state.wordsInBoard.map((word) => <Paper className={this.props.classes.word}>{word}</Paper>)}
    </div>
  }

  select (row, col) {
    this.setState({selected: [row, col]})
  }

  render () {
    const {classes} = this.props
    return <div>

      <AppBar position="static" color="default">
        <Toolbar>
          <Typography variant="h6" color="inherit">
            Nick Baker's Boggle Solver
          </Typography>
        </Toolbar>
      </AppBar>
      <div className={classes.mainBody}>
        <div style={{padding: '20px'}}>
          <div>
            {_.range(0, 4).map(row => (
              <Grid
                container
                spacing={16}
                className={classes.demo}
                alignItems={'center'}
                direction={'row'}
                justify={'center'}>
                {
                  _.range(0, 4).map(col => (
                    <Grid key={row + ' ' + col} item>
                      <Paper className={classes.paper + ' ' + (
                        this.state.selected && this.state.selected[0] === row && this.state.selected[1] === col && classes.selected
                      )} onClick={() => this.select(row, col)}>{this.state.board[row * 4 + col]}</Paper>
                    </Grid>
                  ))
                }
              </Grid>
            ))}
          </div>
          <div style={{padding: '20px'}}>
            <Grid container className={classes.demo} justify="center" spacing={20}>
              <Button variant="contained" color="secondary" className={classes.button} onClick={() => this.randomize()}>
                Randomize
              </Button>
              <Button variant="contained" color="primary" className={classes.button} onClick={() => this.solve()}>
                Solve!
              </Button>
            </Grid>
          </div>
        </div>

        <div style={{flex: 1, padding: '20px'}}>
          {this.solution()}
        </div>
      </div>
    </div>
  }
}

Board.propTypes = {
  classes: PropTypes.object.isRequired,
}

export default withStyles(styles)(Board)