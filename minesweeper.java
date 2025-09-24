//Assignment 9: MINESWEEPER

import java.util.ArrayList;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


//represents an individual cell in the grid
class Cell {
  boolean isMine;
  boolean isRevealed;
  boolean isFlagged;
  ArrayList<Cell> neighbors;
  
  //constructor
  Cell() {
    this.isMine = false;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }
  
  //constructor for testing a mine's status
  Cell(boolean isMine) {
    this.isMine = isMine;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }
  
  //adds a neighbor to this cell
  void addNeighbor(Cell neighbor) {
    this.neighbors.add(neighbor);
  }
  
  //counts the number of neighboring mines this cell has
  int countNeighboringMines() {
    int count = 0;
    for (Cell neighbor : this.neighbors) {
      if (neighbor.isMine) {
        count++;
      }
    }
    return count;
  }
  
  //reveal this cell (left click behavior)
  void reveal() {
    if (!this.isFlagged && !this.isRevealed) {
      this.isRevealed = true;
      
      //then if it has no neighboring mines, reveal all the neighbors
      if (!this.isMine && this.countNeighboringMines() == 0) {
        for (Cell neighbor : this.neighbors) {
          neighbor.reveal();
        }
      }
    }
  }
  
  //toggle flag status (right click behavior)
  void toggleFlag() {
    if (!this.isRevealed) {
      this.isFlagged = !this.isFlagged;
    }   
  }
  
  //draw this cell
  WorldImage drawCell() {
    Color cellColor;
    WorldImage cellImage;
    
    if (!this.isRevealed) {
      //represents a hidden cell, blue
      cellColor = new Color(173, 216, 230);
      cellImage = new RectangleImage(20, 20, OutlineMode.SOLID, cellColor);
      
      //add flag if flagged
      if (this.isFlagged) {
        WorldImage flag = new TriangleImage(new Posn(10, 5), new Posn(10, 15), new Posn(18, 10),
            OutlineMode.SOLID, Color.ORANGE);
        cellImage = new OverlayImage(flag, cellImage);
      }
    }
    else {
      //represents a revealed cell, grey
      cellColor = Color.LIGHT_GRAY;
      cellImage = new RectangleImage(20, 20, OutlineMode.SOLID, cellColor);
      
      if (this.isMine) {
        //represent a mine as a red circle
        WorldImage mine = new CircleImage(8, OutlineMode.SOLID, Color.RED);
        cellImage = new OverlayImage(mine, cellImage);
      }
      else {
        //draws the number of neighboring mines
        int mineCount = this.countNeighboringMines();
        if (mineCount > 0) {
          WorldImage number = new TextImage(Integer.toString(mineCount), 12, Color.BLACK);
          cellImage = new OverlayImage(number, cellImage);
        }
      }
    }  
    
    //add a border to the cells
    WorldImage border = new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK);
    return new OverlayImage(border, cellImage);
  }
  
}

//represents the game logic
class Game {
  int rows;
  int cols;
  int numMines;
  int clickCount;
  ArrayList<ArrayList<Cell>> grid;
  boolean gameOver;
  boolean gameWon;
  Random rand;
  
  //constructor using a seeded random for proper testing
  Game(int rows, int cols, int numMines, Random rand) {
    //constraints for creating a grid:
    //the smallest implementation is a 9 by 9
    //the largest implementation is a 30 by 30
    //the mine count can't be negative or greater than the grid
    if (rows < 9 || rows > 30) {
      throw new IllegalArgumentException("rows must be between 9 and 30 (inclusive)");
    }
    if (cols < 9 || cols > 30) {
      throw new IllegalArgumentException("columns must be between 9 and 30 (inclusive)");
    }
    if (numMines < 0 || numMines >= rows * cols) {
      throw new IllegalArgumentException("number of mines must be between 0 and " + 
    (rows * cols - 1));
    }
    
    this.rows = rows;
    this.cols = cols;
    this.numMines = numMines;
    this.clickCount = 0;
    this.rand = rand;
    this.gameOver = false;
    this.gameWon = false;
    this.initializeGrid();
    this.linkNeighbors();
    this.placeMines();
  }
  
  //convenience constructor for actual gameplay (using an unseeded random)
  Game(int rows, int cols, int numMines) {
    this(rows, cols, numMines, new Random());
  }
  
  //default constructor (30x16 with 99 mines)
  Game() {
    this(16, 30, 99);
  }
  
  //initializes the grid with empty cells
  void initializeGrid() {
    this.grid = new ArrayList<ArrayList<Cell>>();
    for (int i = 0; i < this.rows; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int j = 0; j < this.cols; j++) {
        row.add(new Cell());
      }
      this.grid.add(row);
    }
  }
  
  //links each cell to its neighbors
  void linkNeighbors() {
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        Cell currentCell = this.grid.get(i).get(j);
        
        //checks all possible neighbors
        for (int di = -1; di <= 1; di++) {
          for (int dj = -1; dj <= 1; dj++) {
            if (di == 0 && dj == 0) {
              continue;
            }
            
            int ni = i + di;
            int nj = j + dj;
            
            if (ni >= 0 && ni < this.rows && nj >= 0 && nj < this.cols) {
              currentCell.addNeighbor(this.grid.get(ni).get(nj));
            }
          }
        }
      }
    }    
  }
  
  //randomly places the mines on the grid
  void placeMines() {
    int minesPlaced = 0;
    
    while (minesPlaced < this.numMines) {
      int randomRow = this.rand.nextInt(this.rows);
      int randomCol = this.rand.nextInt(this.cols);
        
      Cell cell = this.grid.get(randomRow).get(randomCol);
        
      //only place mine if cell doesn't already have one
      if (!cell.isMine) {
        cell.isMine = true;
        minesPlaced++;
      }
    }
  }
  
  //handles left mouse click
  void leftClick(int row, int col) {
    if (!this.gameOver && row >= 0 && row < this.rows && col >= 0 && col < this.cols) {
      this.clickCount++;
      Cell clickedCell = this.grid.get(row).get(col);
        
      if (clickedCell.isMine && !clickedCell.isFlagged) {
        //game over, all the mines reveal
        this.gameOver = true;
        this.revealAllMines();
      } 
      else {
        clickedCell.reveal();
        this.checkWinCondition();
      }
    }
  }
  
  //handles right mouse click
  void rightClick(int row, int col) {
    if (!this.gameOver && row >= 0 && row < this.rows && col >= 0 && col < this.cols) {
      Cell clickedCell = this.grid.get(row).get(col);
      clickedCell.toggleFlag();
    }
  }
  
  //reveals all the mines when the game is over
  void revealAllMines() {
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        Cell cell = this.grid.get(i).get(j);
        if (cell.isMine) {
          cell.isRevealed = true;
        }
      }
    }
  } 
  
  //checks if the player won
  void checkWinCondition() {
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        Cell cell = this.grid.get(i).get(j);
        //if any non-mine cell is not revealed, the game is not won
        if (!cell.isMine && !cell.isRevealed) {
          return;
        }
      }
    }
    //all non-mine cells are revealed
    this.gameWon = true;
    this.gameOver = true;
  }
  
  //conditions for restarting the game
  void resetGame() {
    this.gameOver = false;
    this.gameWon = false;
    this.clickCount = 0;
    this.initializeGrid();
    this.linkNeighbors();
    this.placeMines();
  }
  
}

//represents the World for actually displaying the game
class MineWorld extends World {
  Game game;
  int cellSize = 20;
  
  //constructor
  MineWorld(Game game) {
    this.game = game;
  }
  
  //default constructor
  MineWorld() {
    this.game = new Game();
  }
  
  //constructor with custom parameters
  MineWorld(int rows, int cols, int numMines) {
    this.game = new Game(rows, cols, numMines);
  }
  
  //draws the worldScene
  public WorldScene makeScene() {
    int headerHeight = 40;
    int sceneWidth = this.game.cols * this.cellSize;
    int sceneHeight = this.game.rows * this.cellSize + headerHeight;
    WorldScene scene = new WorldScene(sceneWidth, sceneHeight);
    
    //draw the background of the header
    WorldImage headerBackground = new RectangleImage(sceneWidth, headerHeight, 
        OutlineMode.SOLID, Color.LIGHT_GRAY);
    scene.placeImageXY(headerBackground, sceneWidth / 2, headerHeight / 2);
    
    //draws each cell
    for (int i = 0; i < this.game.rows; i++) {
      for (int j = 0; j < this.game.cols; j++) {
        Cell cell = this.game.grid.get(i).get(j);
        WorldImage cellImage = cell.drawCell();
        
        int x = j * this.cellSize + this.cellSize / 2;
        int y = i * this.cellSize + this.cellSize / 2 + headerHeight;
        
        scene.placeImageXY(cellImage, x, y);
      }
    }
    
    //calculate and display mines remaining
    int flaggedCells = 0;
    for (int i = 0; i < this.game.rows; i++) {
      for (int j = 0; j < this.game.cols; j++) {
        if (this.game.grid.get(i).get(j).isFlagged) {
          flaggedCells++;
        }
      }
    }
    
    int minesRemaining = this.game.numMines - flaggedCells;
    //draws the mines remaining counter
    WorldImage mineCounter = new TextImage("Mines remaining: " + minesRemaining, 10, Color.BLACK);
    scene.placeImageXY(mineCounter, 55, headerHeight / 2);
    
    //draw click counter
    WorldImage clickCounter = new TextImage("Clicks: " + this.game.clickCount, 10, Color.BLACK);
    scene.placeImageXY(clickCounter, sceneWidth - 35, headerHeight / 2);
    
    //draws end of game message
    if (this.game.gameOver) {
      String message;
      Color messageColor;
      
      if (this.game.gameWon) {
        message = "You Win!";
        messageColor = Color.GREEN;
      } 
      else {
        message = "Game Over!";
        messageColor = Color.RED;
      }
      
      WorldImage gameOverText = new TextImage(message, 24, messageColor);
      scene.placeImageXY(gameOverText, sceneWidth / 2, sceneHeight / 2);
      
      WorldImage restartText = new TextImage("press 'R' to restart", 14, Color.RED);
      scene.placeImageXY(restartText, sceneWidth / 2, sceneHeight / 2 + 20);
    }
    return scene;
  }
  
  //handles mouse clicks
  public void onMouseClicked(Posn pos, String buttonName) {
    int headerHeight = 40;
    int adjustedY = pos.y - headerHeight;
    
    //only process clicks on the grid area (not header)
    if (adjustedY >= 0) {
      int col = pos.x / this.cellSize;
      int row = adjustedY / this.cellSize;
      
      if (buttonName.equals("LeftButton")) {
        this.game.leftClick(row, col);
      } 
      else if (buttonName.equals("RightButton")) {
        this.game.rightClick(row, col);
      }
    }
  }
  
  //restarts the game when 'r' is pressed and game is over
  public void onKeyEvent(String key) {
    if (key.equals("r") && this.game.gameOver) {
      this.game.resetGame();
    }
  }
  
}

//test class!!
class Examples {
  //test with a small grid for easier testing
  Random testRand = new Random(42);
  Game testGame = new Game(9, 9, 3, testRand);
  MineWorld testWorld = new MineWorld(testGame);
  
  //test cell functionality
  Cell cell1 = new Cell();
  Cell cell2 = new Cell(true); //mine
  Cell cell3 = new Cell();
  
  //test building the game
  void testGame(Tester t) {
    //test game initialization
    t.checkExpect(testGame.rows, 9);
    t.checkExpect(testGame.cols, 9);
    t.checkExpect(testGame.numMines, 3);
    t.checkExpect(testGame.gameOver, false);
    t.checkExpect(testGame.gameWon, false);
      
    //test that mines were placed
    int mineCount = 0;
    for (int i = 0; i < testGame.rows; i++) {
      for (int j = 0; j < testGame.cols; j++) {
        if (testGame.grid.get(i).get(j).isMine) { 
          mineCount++;
        }
      }
    }
    t.checkExpect(mineCount, 3);
  }
  
  //test invalid grid size
  void testInvalidGridSize(Tester t) {
    Random testRand = new Random(42);
    //test rows too small (less than 9)
    t.checkConstructorException(new IllegalArgumentException("rows must be between 9 and 30 "
        + "(inclusive)"), "Game", 5, 15, 10, testRand);
    //test rows too large (greater than 30)
    t.checkConstructorException(new IllegalArgumentException("rows must be between 9 and 30 "
        + "(inclusive)"), "Game", 31, 15, 20, testRand);
    //test columns too small (less than 9)
    t.checkConstructorException(new IllegalArgumentException("columns must be between 9 and 30 "
        + "(inclusive)"), "Game", 15, 8, 10, testRand);
    //test columns too large (greater than 30)
    t.checkConstructorException(new IllegalArgumentException("columns must be between 9 and 30 "
        + "(inclusive)"), "Game", 15, 31, 25, testRand);
    //test both rows and columns invalid (should catch rows first)
    t.checkConstructorException(new IllegalArgumentException("rows must be between 9 and 30 "
        + "(inclusive)"), "Game", 8, 8, 5, testRand);
  }
  
  //test invalid mine count
  void testInvalidMineCount(Tester t) {
    Random testRand = new Random(42);   
    //test negative mine count
    t.checkConstructorException(new IllegalArgumentException("number of mines must be between "
        + "0 and 149"), "Game", 10, 15, -5, testRand);
    //test mine count too large (>= total cells)
    t.checkConstructorException(new IllegalArgumentException("number of mines must be between "
        + "0 and 80"), "Game", 9, 9, 81, testRand); 
    //test mine count equal to total cells
    t.checkConstructorException(new IllegalArgumentException("number of mines must be between "
        + "0 and 199"), "Game", 10, 20, 200, testRand);
  }
  
  //test the mine counter
  void testMineCounter(Tester t) {
    Game counterGame = new Game(9, 9, 2, new Random(30));   
    //initially no cells should be flagged
    int initialFlagged = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (counterGame.grid.get(i).get(j).isFlagged) {
          initialFlagged++;
        }
      }
    }
    t.checkExpect(initialFlagged, 0);
    
    //flag one cell
    counterGame.rightClick(0, 0);
    int flaggedAfterOne = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (counterGame.grid.get(i).get(j).isFlagged) {
          flaggedAfterOne++;
        }
      }
    }
    t.checkExpect(flaggedAfterOne, 1);
    
    //flag second cell
    counterGame.rightClick(1, 1);
    int flaggedAfterTwo = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (counterGame.grid.get(i).get(j).isFlagged) {
          flaggedAfterTwo++;
        }
      }
    }
    t.checkExpect(flaggedAfterTwo, 2);
    
    //unflag one cell
    counterGame.rightClick(0, 0);
    int flaggedAfterUnflag = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (counterGame.grid.get(i).get(j).isFlagged) {
          flaggedAfterUnflag++;
        }
      }
    }
    t.checkExpect(flaggedAfterUnflag, 1);
    
    //test mine counter calculation
    int totalMines = counterGame.numMines;
    int currentFlagged = flaggedAfterUnflag;
    int expectedRemaining = totalMines - currentFlagged;
    t.checkExpect(expectedRemaining, 1);
  }
  
  //test the click counter
  void testClickCounter(Tester t) {
    Game clickTestGame = new Game(9, 9, 0, new Random(42));
    t.checkExpect(clickTestGame.clickCount, 0);    
    //test left click increments counter
    clickTestGame.leftClick(4, 4);
    t.checkExpect(clickTestGame.clickCount, 1);   
    //flagging doesn't count
    clickTestGame.rightClick(3, 3);
    t.checkExpect(clickTestGame.clickCount, 1);   
    //test that invalid clicks
    int beforeInvalidClick = clickTestGame.clickCount;
    clickTestGame.leftClick(-1, 5);
    clickTestGame.rightClick(20, 20);
    t.checkExpect(clickTestGame.clickCount, beforeInvalidClick);
  }
  
  //test game restart
  void testRestart(Tester t) {
    Game restartGame = new Game(9, 9, 5, new Random(123));
    
    //simulate ending the game
    restartGame.gameOver = true;
    restartGame.gameWon = false;
    restartGame.clickCount = 10;
    
    //reset the game
    restartGame.resetGame();
    
    //check reset state
    t.checkExpect(restartGame.gameOver, false);
    t.checkExpect(restartGame.gameWon, false);
    t.checkExpect(restartGame.clickCount, 0);
    
    //verify mines are still present (should be 5 total)
    int mineCount = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (restartGame.grid.get(i).get(j).isMine) {
          mineCount++;
        }
      }
    }
    t.checkExpect(mineCount, 5);
    
    //verify all cells are in initial state
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        Cell cell = restartGame.grid.get(i).get(j);
        t.checkExpect(cell.isRevealed, false);
        t.checkExpect(cell.isFlagged, false);
      }
    }
  }
  
  //test onkey for restart
  void testOnKeyEvent(Tester t) {
    MineWorld keyWorld = new MineWorld(9, 9, 0);
    
    //end the game
    keyWorld.game.gameOver = true;
    keyWorld.game.gameWon = true;
    keyWorld.game.clickCount = 5;
    
    //test that 'r' key resets the game
    keyWorld.onKeyEvent("r");
    t.checkExpect(keyWorld.game.gameOver, false);
    t.checkExpect(keyWorld.game.gameWon, false);
    t.checkExpect(keyWorld.game.clickCount, 0);
    
    //test that other keys don't reset
    keyWorld.game.gameOver = true;
    keyWorld.onKeyEvent("x");
    t.checkExpect(keyWorld.game.gameOver, true);
    
    //test that 'r' doesn't reset during active game
    keyWorld.game.gameOver = false;
    keyWorld.game.clickCount = 3;
    keyWorld.onKeyEvent("r");
    t.checkExpect(keyWorld.game.clickCount, 3);
  }
  
  //test cellNeighbors & countNeighboringMines
  void testCellNeighbors(Tester t) {
    //set up test cells with neighbors
    cell1.addNeighbor(cell2);
    cell1.addNeighbor(cell3);
      
    //test countNeighboringMines
    t.checkExpect(cell1.countNeighboringMines(), 1);
    t.checkExpect(cell2.countNeighboringMines(), 0);
    t.checkExpect(cell3.countNeighboringMines(), 0);
  }
  
  //test addNeighbor
  void testAddNeighbor(Tester t) {
    Cell testCell = new Cell();
    Cell neighbor1 = new Cell();
    Cell neighbor2 = new Cell(true); //mine
    Cell neighbor3 = new Cell();
    
    t.checkExpect(testCell.neighbors.size(), 0);
    testCell.addNeighbor(neighbor1);
    t.checkExpect(testCell.neighbors.size(), 1);
    t.checkExpect(testCell.neighbors.get(0), neighbor1);
    testCell.addNeighbor(neighbor2);
    t.checkExpect(testCell.neighbors.size(), 2);
    t.checkExpect(testCell.neighbors.get(1), neighbor2);
    testCell.addNeighbor(neighbor3);
    t.checkExpect(testCell.neighbors.size(), 3);
    t.checkExpect(testCell.neighbors.get(2), neighbor3);
    t.checkExpect(testCell.countNeighboringMines(), 1);
  }
  
  //test reveal
  void testReveal(Tester t) {
    //test basic reveal functionality
    Cell basicCell = new Cell();
    t.checkExpect(basicCell.isRevealed, false);
    basicCell.reveal();
    t.checkExpect(basicCell.isRevealed, true);
    
    //test that flagged cells don't reveal
    Cell flaggedCell = new Cell();
    flaggedCell.toggleFlag();
    t.checkExpect(flaggedCell.isFlagged, true);
    t.checkExpect(flaggedCell.isRevealed, false);
    flaggedCell.reveal();
    t.checkExpect(flaggedCell.isRevealed, false); //should still be hidden
    
    //test that already revealed cells stay revealed
    Cell alreadyRevealed = new Cell();
    alreadyRevealed.reveal();
    t.checkExpect(alreadyRevealed.isRevealed, true);
    alreadyRevealed.reveal(); //reveal again
    t.checkExpect(alreadyRevealed.isRevealed, true); //should still be revealed
    
    //test mine cell reveal doesn't trigger flood fill
    Cell mineCell = new Cell(true);
    Cell mineNeighbor = new Cell();
    mineCell.addNeighbor(mineNeighbor);
    t.checkExpect(mineCell.isRevealed, false);
    t.checkExpect(mineNeighbor.isRevealed, false);
    mineCell.reveal();
    t.checkExpect(mineCell.isRevealed, true);
    t.checkExpect(mineNeighbor.isRevealed, false); //neighbor should not be revealed
    
    //test cell with neighboring mines doesn't trigger flood fill
    Cell cellWithMineNeighbors = new Cell();
    Cell mineNeighbor1 = new Cell(true);
    Cell normalNeighbor1 = new Cell();
    cellWithMineNeighbors.addNeighbor(mineNeighbor1);
    cellWithMineNeighbors.addNeighbor(normalNeighbor1);
    
    t.checkExpect(cellWithMineNeighbors.isRevealed, false);
    t.checkExpect(normalNeighbor1.isRevealed, false);
    cellWithMineNeighbors.reveal();
    t.checkExpect(cellWithMineNeighbors.isRevealed, true);
    t.checkExpect(normalNeighbor1.isRevealed, false); //shouldn't flood fill
  }
  
  //test toggleFlag
  void testToggleFlag(Tester t) {
    //test basic flag toggling on hidden cell
    Cell basicCell = new Cell();
    t.checkExpect(basicCell.isFlagged, false);
    t.checkExpect(basicCell.isRevealed, false);
    
    //first toggle should flag the cell
    basicCell.toggleFlag();
    t.checkExpect(basicCell.isFlagged, true);
    t.checkExpect(basicCell.isRevealed, false); //should still be hidden
    
    //second toggle should unflag the cell
    basicCell.toggleFlag();
    t.checkExpect(basicCell.isFlagged, false);
    t.checkExpect(basicCell.isRevealed, false); //should still be hidden
    
    //test that revealed cells cannot be flagged
    Cell revealedCell = new Cell();
    revealedCell.reveal(); //reveal first
    t.checkExpect(revealedCell.isRevealed, true);
    t.checkExpect(revealedCell.isFlagged, false);
    
    //try to flag revealed cell - should do nothing
    revealedCell.toggleFlag();
    t.checkExpect(revealedCell.isFlagged, false); //should remain unflagged
    t.checkExpect(revealedCell.isRevealed, true); //should remain revealed
    
    //test flagging mine cells (should work same as normal cells)
    Cell mineCell = new Cell(true);
    t.checkExpect(mineCell.isMine, true);
    t.checkExpect(mineCell.isFlagged, false);
    t.checkExpect(mineCell.isRevealed, false);
    
    //flag the mine
    mineCell.toggleFlag();
    t.checkExpect(mineCell.isFlagged, true);
    t.checkExpect(mineCell.isRevealed, false);
    t.checkExpect(mineCell.isMine, true); //should still be a mine
    
    //unflag the mine
    mineCell.toggleFlag();
    t.checkExpect(mineCell.isFlagged, false);
    t.checkExpect(mineCell.isRevealed, false);
    t.checkExpect(mineCell.isMine, true);
    
    //test revealed mine cannot be flagged
    Cell revealedMine = new Cell(true);
    revealedMine.reveal();
    t.checkExpect(revealedMine.isRevealed, true);
    t.checkExpect(revealedMine.isFlagged, false);
    t.checkExpect(revealedMine.isMine, true);
    
    revealedMine.toggleFlag(); //should do nothing
    t.checkExpect(revealedMine.isFlagged, false);
    t.checkExpect(revealedMine.isRevealed, true);
    t.checkExpect(revealedMine.isMine, true);
  }
  
  //test drawCell
  void testDrawCell(Tester t) {
    //test drawing hidden unflagged cell
    Cell hiddenCell = new Cell();
    WorldImage hiddenImage = hiddenCell.drawCell();
    t.checkExpect(hiddenImage != null, true); //should return a valid image
    
    //test drawing hidden flagged cell
    Cell flaggedCell = new Cell();
    flaggedCell.toggleFlag();
    WorldImage flaggedImage = flaggedCell.drawCell();
    t.checkExpect(flaggedImage != null, true); //should return a valid image
    
    //test drawing revealed empty cell (no neighboring mines)
    Cell emptyRevealedCell = new Cell();
    emptyRevealedCell.reveal();
    WorldImage emptyImage = emptyRevealedCell.drawCell();
    t.checkExpect(emptyImage != null, true); //should return a valid image
    
    //test drawing revealed cell with neighboring mines
    Cell cellWithMineNeighbors = new Cell();
    Cell mine1 = new Cell(true);
    Cell mine2 = new Cell(true);
    Cell mine3 = new Cell(true);
    cellWithMineNeighbors.addNeighbor(mine1);
    cellWithMineNeighbors.addNeighbor(mine2);
    cellWithMineNeighbors.addNeighbor(mine3);
    cellWithMineNeighbors.reveal();
    
    WorldImage numberedImage = cellWithMineNeighbors.drawCell();
    t.checkExpect(numberedImage != null, true); //should return a valid image
    
    //test drawing revealed mine cell
    Cell revealedMine = new Cell(true);
    revealedMine.reveal();
    WorldImage mineImage = revealedMine.drawCell();
    t.checkExpect(mineImage != null, true); //should return a valid image
    
    //test drawing hidden mine cell (should look like normal hidden cell)
    Cell hiddenMine = new Cell(true);
    WorldImage hiddenMineImage = hiddenMine.drawCell();
    t.checkExpect(hiddenMineImage != null, true); //should return a valid image
    
    //test drawing flagged mine cell
    Cell flaggedMine = new Cell(true);
    flaggedMine.toggleFlag();
    WorldImage flaggedMineImage = flaggedMine.drawCell();
    t.checkExpect(flaggedMineImage != null, true); //should return a valid image
    
    //test various mine count scenarios
    for (int mineCount = 1; mineCount <= 8; mineCount++) {
      Cell testCell = new Cell();
      
      //add the specified number of mine neighbors
      for (int i = 0; i < mineCount; i++) {
        testCell.addNeighbor(new Cell(true));
      }
      
      testCell.reveal();
      WorldImage countImage = testCell.drawCell();
      t.checkExpect(countImage != null, true); //should return valid image
    }
  }
  
  //test initializeGrid
  void testInitializeGrid(Tester t) {
    Game gridGame = new Game(9, 10, 0, new Random(25));
    
    //test grid dimensions
    t.checkExpect(gridGame.grid.size(), 9); //4 rows
    t.checkExpect(gridGame.grid.get(0).size(), 10); //5 columns
    
    //test all cells are initialized properly
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 10; j++) {
        Cell cell = gridGame.grid.get(i).get(j);
        t.checkExpect(cell.isMine, false);
        t.checkExpect(cell.isRevealed, false);
        t.checkExpect(cell.isFlagged, false);
      }
    }
  }
  
  //test linkNeighbors
  void testLinkNeighbors(Tester t) {
    Game smallGame = new Game(9, 9, 0, new Random(1));
    
    //test corner cell has 3 neighbors
    Cell cornerCell = smallGame.grid.get(0).get(0);
    t.checkExpect(cornerCell.neighbors.size(), 3);
    
    //test center cell has 8 neighbors
    Cell centerCell = smallGame.grid.get(4).get(4);
    t.checkExpect(centerCell.neighbors.size(), 8);
    
    //test edge cell has 5 neighbors
    Cell edgeCell = smallGame.grid.get(0).get(4);
    t.checkExpect(edgeCell.neighbors.size(), 5);
  } 
  
  //test placeMines
  void testPlaceMines(Tester t) {
    Game mineTestGame = new Game(9, 9, 5, new Random(100));
    
    //count mines placed
    int totalMines = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (mineTestGame.grid.get(i).get(j).isMine) {
          totalMines++;
        }
      }
    }
    t.checkExpect(totalMines, 5);
  }
  
  //test leftClick
  void testLeftClick(Tester t) {
    Game clickGame = new Game(9, 9, 0, new Random(5)); //no mines for safe testing
    
    //test clicking reveals cell
    t.checkExpect(clickGame.grid.get(4).get(4).isRevealed, false);
    clickGame.leftClick(4, 4);
    t.checkExpect(clickGame.grid.get(4).get(4).isRevealed, true);
    
    //test clicking on mine ends game
    Game mineGame = new Game(9, 9, 1, new Random(10));
    //find the mine
    boolean foundMine = false;
    for (int i = 0; i < 9 && !foundMine; i++) {
      for (int j = 0; j < 9 && !foundMine; j++) {
        if (mineGame.grid.get(i).get(j).isMine) {
          t.checkExpect(mineGame.gameOver, false);
          mineGame.leftClick(i, j);
          t.checkExpect(mineGame.gameOver, true);
          foundMine = true;
        }
      }
    }
    //test clicking flagged cell doesn't reveal it
    Game flagGame = new Game(9, 9, 0, new Random(7));
    Cell flaggedCell = flagGame.grid.get(0).get(0);
    flaggedCell.toggleFlag();
    t.checkExpect(flaggedCell.isRevealed, false);
    flagGame.leftClick(0, 0);
    t.checkExpect(flaggedCell.isRevealed, false);
  }
  
  //test rightClick
  void testRightClick(Tester t) {
    Game rightGame = new Game(9, 9, 0, new Random(8));
    Cell testCell = rightGame.grid.get(4).get(4);
    
    //test right click flags cell
    t.checkExpect(testCell.isFlagged, false);
    rightGame.rightClick(4, 4);
    t.checkExpect(testCell.isFlagged, true);
    
    //test right click again unflags cell
    rightGame.rightClick(4, 4);
    t.checkExpect(testCell.isFlagged, false);
    
    //test right clicking revealed cell does nothing
    testCell.reveal();
    rightGame.rightClick(4, 4);
    t.checkExpect(testCell.isFlagged, false); //should remain unflagged
  }
  
  //test revealAllMines
  void testRevealAllMines(Tester t) {
    Game minesGame = new Game(9, 9, 2, new Random(15));
    
    //initially mines should be hidden
    int revealedMines = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        Cell cell = minesGame.grid.get(i).get(j);
        if (cell.isMine && cell.isRevealed) {
          revealedMines++;
        }
      }
    }
    t.checkExpect(revealedMines, 0);
    
    //after calling revealAllMines all mines should be revealed
    minesGame.revealAllMines();
    revealedMines = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        Cell cell = minesGame.grid.get(i).get(j);
        if (cell.isMine && cell.isRevealed) {
          revealedMines++;
        }
      }
    }
    t.checkExpect(revealedMines, 2); //should reveal both mines
  }
  
  //check winCondition
  void testCheckWinCondition(Tester t) {
    Game winGame = new Game(9, 9, 1, new Random(20));
    
    //game should not be won initially
    t.checkExpect(winGame.gameWon, false);
    t.checkExpect(winGame.gameOver, false);
    
    //reveal all non-mine cells
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        Cell cell = winGame.grid.get(i).get(j);
        if (!cell.isMine) {
          cell.isRevealed = true;
        }
      }
    }
    
    //now check win condition
    winGame.checkWinCondition();
    t.checkExpect(winGame.gameWon, true);
    t.checkExpect(winGame.gameOver, true);
  }
  
  //test floodFill
  void testFloodFill(Tester t) {
    Game floodGame = new Game(9, 9, 0, new Random(50)); //no mines
    
    //center cell click should reveal multiple cells due to flood fill
    floodGame.leftClick(1, 1);    
    //check that multiple cells are revealed
    int revealedCount = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        if (floodGame.grid.get(i).get(j).isRevealed) {
          revealedCount++;
        }
      }
    }
    //should reveal more than just the clicked cell
    t.checkExpect(revealedCount > 1, true);
  }
  
  //test makeScene
  void testMakeScene(Tester t) {
    //test that makeScene returns a valid WorldScene
    WorldScene scene = testWorld.makeScene();
    t.checkExpect(scene != null, true);
    
    //test scene dimensions
    int expectedWidth = testWorld.game.cols * testWorld.cellSize;
    int expectedHeight = testWorld.game.rows * testWorld.cellSize + 40;
    t.checkExpect(scene.width, expectedWidth);
    t.checkExpect(scene.height, expectedHeight);
    
    //test active game scene (no game over)
    MineWorld activeWorld = new MineWorld(9, 9, 2);
    activeWorld.game.gameOver = false;
    activeWorld.game.gameWon = false;
    WorldScene activeScene = activeWorld.makeScene();
    t.checkExpect(activeScene != null, true);
    t.checkExpect(activeScene.width, 9 * 20);
    t.checkExpect(activeScene.height, 9 * 20 + 40);
    
    //test game over with win condition
    MineWorld winWorld = new MineWorld(9, 9, 1);
    winWorld.game.gameOver = true;
    winWorld.game.gameWon = true;
    WorldScene winScene = winWorld.makeScene();
    t.checkExpect(winScene != null, true);
    
    //test game over with loss condition
    MineWorld loseWorld = new MineWorld(9, 9, 1);
    loseWorld.game.gameOver = true;
    loseWorld.game.gameWon = false;
    WorldScene loseScene = loseWorld.makeScene();
    t.checkExpect(loseScene != null, true);
    
    //test mines remaining counter with no flags
    MineWorld counterWorld = new MineWorld(9, 9, 5);
    WorldScene noFlagsScene = counterWorld.makeScene();
    t.checkExpect(noFlagsScene != null, true);
    //should show 5 mines remaining
    
    //test mines remaining counter with some flags
    MineWorld flagWorld = new MineWorld(9, 9, 5);
    flagWorld.game.rightClick(0, 0); // flag one cell
    flagWorld.game.rightClick(1, 1); // flag another cell
    WorldScene flaggedScene = flagWorld.makeScene();
    t.checkExpect(flaggedScene != null, true);
    //should show 3 mines remaining (5 - 2 flags)
    
    //test click counter display
    MineWorld clickWorld = new MineWorld(9, 9, 0);
    clickWorld.game.leftClick(4, 4); // make 1 click
    clickWorld.game.leftClick(3, 3); // make 2nd click
    clickWorld.game.leftClick(2, 2); // make 3rd click
    WorldScene clickScene = clickWorld.makeScene();
    t.checkExpect(clickScene != null, true);
    //should show 3 clicks
    
    //test minimum grid size (9x9)
    MineWorld minWorld = new MineWorld(9, 9, 1);
    WorldScene minScene = minWorld.makeScene();
    t.checkExpect(minScene != null, true);
    t.checkExpect(minScene.width, 9 * 20);
    t.checkExpect(minScene.height, 9 * 20 + 40);
    
    //test larger grid size
    MineWorld largeWorld = new MineWorld(15, 20, 10);
    WorldScene largeScene = largeWorld.makeScene();
    t.checkExpect(largeScene != null, true);
    t.checkExpect(largeScene.width, 20 * 20); // 20 cols * 20 cellSize
    t.checkExpect(largeScene.height, 15 * 20 + 40); // 15 rows * 20 + header
    
    //test with zero mines
    MineWorld noMineWorld = new MineWorld(9, 9, 0);
    WorldScene noMineScene = noMineWorld.makeScene();
    t.checkExpect(noMineScene != null, true);
    //should show 0 mines remaining
    
    //test scene with various cell states
    MineWorld mixedWorld = new MineWorld(9, 9, 2);
    //flag some cells
    mixedWorld.game.rightClick(0, 0);
    mixedWorld.game.rightClick(1, 0);
    //reveal some cells
    mixedWorld.game.leftClick(8, 8); //safe corner
    mixedWorld.game.leftClick(7, 8); //another safe cell
    WorldScene mixedScene = mixedWorld.makeScene();
    t.checkExpect(mixedScene != null, true);
    
    //test negative mines remaining (more flags than mines)
    MineWorld overFlagWorld = new MineWorld(9, 9, 2);
    overFlagWorld.game.rightClick(0, 0); //flag 1
    overFlagWorld.game.rightClick(1, 0); //flag 2  
    overFlagWorld.game.rightClick(2, 0); //flag 3
    overFlagWorld.game.rightClick(3, 0); //flag 4
    WorldScene overFlagScene = overFlagWorld.makeScene();
    t.checkExpect(overFlagScene != null, true);
    //should show -2 mines remaining (2 - 4 flags)
    
    //test scene after game reset
    MineWorld resetWorld = new MineWorld(9, 9, 3);
    resetWorld.game.gameOver = true;
    resetWorld.game.clickCount = 10;
    resetWorld.game.resetGame(); // reset the game
    WorldScene resetScene = resetWorld.makeScene();
    t.checkExpect(resetScene != null, true);
    //should show 0 clicks and 3 mines remaining
    
    //test scene with revealed mines (game over state)
    MineWorld mineRevealWorld = new MineWorld(9, 9, 1);
    //find and click the mine to end game
    boolean foundMine = false;
    for (int i = 0; i < 9 && !foundMine; i++) {
      for (int j = 0; j < 9 && !foundMine; j++) {
        if (mineRevealWorld.game.grid.get(i).get(j).isMine) {
          mineRevealWorld.game.leftClick(i, j);
          foundMine = true;
        }
      }
    }
    WorldScene mineRevealScene = mineRevealWorld.makeScene();
    t.checkExpect(mineRevealScene != null, true);
    t.checkExpect(mineRevealWorld.game.gameOver, true);
    
    //test scene with all cells revealed except mines (win condition)
    MineWorld almostWinWorld = new MineWorld(9, 9, 1);
    //reveal all non-mine cells
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        Cell cell = almostWinWorld.game.grid.get(i).get(j);
        if (!cell.isMine) {
          cell.isRevealed = true;
        }
      }
    }
    almostWinWorld.game.checkWinCondition();
    WorldScene winConditionScene = almostWinWorld.makeScene();
    t.checkExpect(winConditionScene != null, true);
    t.checkExpect(almostWinWorld.game.gameWon, true);
    t.checkExpect(almostWinWorld.game.gameOver, true);
  }
  
  //test onMouseClicked
  void testOnMouseClicked(Tester t) {
    //test left click
    MineWorld clickWorld = new MineWorld(9, 9, 0); //no mines
    Cell testCell = clickWorld.game.grid.get(1).get(1);
    t.checkExpect(testCell.isRevealed, false);
    
    //simulate left click at position (30, 70) which should be cell (1, 1)
    Posn clickPos = new Posn(30, 70);
    clickWorld.onMouseClicked(clickPos, "LeftButton"); //changed from testWorld to clickWorld
    t.checkExpect(testCell.isRevealed, true);
    
    //test right click, create separate world to avoid state conflicts
    MineWorld flagWorld = new MineWorld(9, 9, 0);
    Cell flagCell = flagWorld.game.grid.get(0).get(0);
    t.checkExpect(flagCell.isFlagged, false);
    
    //simulate right click at position (10, 50) which should be cell (0, 0)
    Posn flagPos = new Posn(10, 50);
    flagWorld.onMouseClicked(flagPos, "RightButton");
    t.checkExpect(flagCell.isFlagged, true);
  }
  
  //run the game!!
  void testBigBang(Tester t) {
    MineWorld world = new MineWorld(15, 15, 10);
    int headerHeight = 40;
    world.bigBang(world.game.cols * world.cellSize, 
        world.game.rows * world.cellSize + headerHeight, 0.1);
  }
    
}