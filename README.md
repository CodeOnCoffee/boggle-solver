## Introduction
Boggle Solver

### Overview
This Boggle solver is a combination of a React Single-Page app and a Java backend. Both are hosted by Spring Boot.

The Backend Solver searches a database of 272,000 possible words to find matches on a given board. This search is run 
across multiple threads concurrently

## Running

### Option 1: Direct from Source
> ./gradlew bootRun

### Option2: Docker Image
> docker run -p 8080:8080 codeoncoffee/boggle-solver:latest 

Open browser to http://localhost:8080

## TODOS:
1. Support for "QU". The backend was specifically implemented with String instead of Char in order to support this in future versions.
2. Image endpoint. Post an image to run OCR software on. Should be possible to support mobile applications with the same backend.
3. Cleanup frontend dependencies. Based on a previous code sample. Many of the dependencies can be removed
