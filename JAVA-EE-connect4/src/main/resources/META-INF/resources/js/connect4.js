var NROWS = 5;
var NCOLS = 7;
var BW = 800;
var BH = 500;
var CW = (BW - 20.0) / NCOLS;
var CH = (BH - 20.0) / NROWS;
var numMoves;
var shape = Array(2);
shape[0] = document.getElementById("shapeRed");
shape[1] = document.getElementById("shapeBlue");
var canvas = document.getElementById("canvas");
canvas.addEventListener("mousedown", doMouseDown, false);
var gameOver, board, onMove, won, thisPlayer;
var lastStarted = 1;
var ctx = canvas.getContext("2d");
var myID;
var thisPlayer;
startGame();

window.onload = function ()
{
    connect();
}

var wsocket;
function connect()
{
    wsocket = new WebSocket("ws://localhost:8080/connect4-1.0-SNAPSHOT/game ")
    wsocket.onmessage = onMessage;
}



function onMessage(evt)
{
    if(gameOver)
    {
        restartGame();
    }
    console.log("msg " + evt.data);
    let data = evt.data.split(";");
    myID = data[0];
    let r = data[1];
    let c = data[2];
    won = data[3];
    if(r >= 0) {
        board[r][c] = onMove;
        ctx.drawImage(shape[onMove], 17 + CW * c, 14 + CH * r);
        onMove = 1 - onMove;
    }
    else if (won == -2) thisPlayer = 0;

    if(won >= -1)
    {
        gameOver = true;
        console.log("calling showWinner(), won=" + won);
        showWinner();
    }
}

function startGame()
{
    board = createBoard();
    thisPlayer = 1;
    myID = -1;
    restartGame();
}

function restartGame()
{
    gameOver = false;
    onMove = 1 -lastStarted;
    lastStarted = onMove;
    numMoves = 0;
    for (var r = 0; r < NROWS; r++)
        for (var c = 0; c < NCOLS; c++)
            board[r][c] = -1;
    drawBoard();
}

function createBoard()
{
    var b = new Array(NROWS);
    for (var r = 0; r < NROWS; r++)
        b[r] = new Array(NCOLS);
    return b;
}

function doMouseDown(event)
{
    console.log("this: " + thisPlayer + ", onMove: " + onMove);
    if (gameOver)
    {
        restartGame();
        return;
    }
    if (onMove != thisPlayer) return;
    r = Math.floor((event.pageY - 10 - canvas.offsetTop) / CH);
    c = Math.floor((event.pageX - 10 - canvas.offsetLeft) / CW);
    if (board[r][c] != -1) return;
    if (r < NROWS - 1)
        if (board[r + 1][c] == -1) return;
    board[r][c] = onMove;
    ctx.drawImage(shape[onMove], 17 + CW * c, 14 + CH * r);
    numMoves++;
    wsocket.send(myID + ";" + r + ";" + c);
    onMove = 1 - onMove;
}

function showWinner()
{
    var msg = "";
    console.log("in showWinner(), won=" + won + ", msg:" + msg);
    ctx.font="bold 40px Georgia";
    ctx.fillStyle="#D0D080";
    switch (Number(won))
    {
        case -1: msg = "Draw :-("; break;
        case 0: msg = "Red won!"; break;
        case 1: msg = "Blue won!"; break;
    }
    ctx.fillText(msg, BW / 2 - 100, BH / 2);
}

function drawBoard()
{
    ctx.fillStyle="#203010";
    ctx.fillRect(10, 10, BW - 20, BH - 20);
    fancyBorder(0, 10, 10, BH, 0);
    fancyBorder(0, 0, BW - 10, 10, 1);
    fancyBorder(BW - 10, 0, 10, BH - 10, 0);
    fancyBorder(10, BH - 10, BW - 10, 10, 1);
    drawGrid();
}

function fancyBorder(x, y, w, h, orient)
{
    if (orient == 0) var grd = ctx.createLinearGradient(x, y, x + w, y);
    else var grd = ctx.createLinearGradient(x, y, x, y + h);
    grd.addColorStop(0, "#106010");
    grd.addColorStop(0.5, "#909030");
    grd.addColorStop(1, "#106010");
    ctx.fillStyle = grd;
    ctx.fillRect(x, y, w, h);
}

function drawGrid()
{
    ctx.strokeStyle="#102010";
    for (var col = 1; col < NCOLS; col++)
    {
        ctx.beginPath();
        ctx.moveTo(10 + col * CW, 10);
        ctx.lineTo(10 + col * CW, BH - 10);
        ctx.stroke();
    }
    for (var row = 1; row < NROWS; row++)
    {
        ctx.beginPath();
        ctx.moveTo(10, 10 + row * CH);
        ctx.lineTo(BW - 10, 10 + row * CH);
        ctx.stroke();
    }
}
