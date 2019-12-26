function checkState(state) {
    if(state != "PLAY") {
        document.getElementById("header").innerHTML=state;
    }
}

function updateScore() {
    var request = new XMLHttpRequest()
    request.open('GET', 'http://localhost:8080/chess/score', true)
    request.onload = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                var data = JSON.parse(this.response)
               
                document.getElementById("scoreWhite").innerHTML= "WHITE: " + data.scoreWhite;
                document.getElementById("scoreBlack").innerHTML= "BLACK: " + data.scoreBlack;
            
            }
            else {
                console.error(request.statusText)
            }
        }
    };
    request.send();
}

function makeIntelligentMove(color) {
    var request = new XMLHttpRequest()
    request.open('PATCH', 'http://localhost:8080/chess/figure/move/intelligent/' + color, true)
    request.onreadystatechange = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                var data = JSON.parse(this.response)
                console.log(data.state)
                checkState(data.state);
                reload();
            
            }
            else {
                console.error(request.statusText)
            }
        }
    };
    request.send();
}

function makeRandomMove(color) {
    var request = new XMLHttpRequest()
    request.open('PATCH', 'http://localhost:8080/chess/figure/move/random/' + color, true)
    request.onload = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                var data = JSON.parse(this.response)
                console.log(data.state)
                checkState(data.state);
                reload();
            
            }
            else {
                console.error(request.statusText)
            }
        }
    };
    request.send();
}

function onDrop (source, target, piece, newPos, oldPos, orientation) {

    var request = new XMLHttpRequest()

    // Open a new connection, using the GET request on the URL endpoint
    request.open('PATCH', 'http://localhost:8080/chess/figure/move/' + source + '/' + target, true)
    request.onreadystatechange = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                var data = JSON.parse(this.response)
                console.log(data)
                reload();
                
            }
            else {
                console.error(request.statusText)
            }
        }
    };
    request.send();
    window.setTimeout(makeIntelligentMove("black"), 500)
}

$('#resetBoard').on('click', function () {
    var request = new XMLHttpRequest()
    request.open('PUT', 'http://localhost:8080/chess/reset', true)
    request.send();
    reload();
    
})

function reload() {
    var request = new XMLHttpRequest()
    request.open('GET', 'http://localhost:8080/chess/figure/frontend', true)
    request.onload = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                var data = JSON.parse(this.response)
                console.log(data.renderBoard)
                board.position(data.renderBoard)
                updateScore();
            }
            else {
                console.error(request.statusText)
            }
        }
    };
    request.send();
}

function greySquare (square) {

  var whiteSquareGrey = 'blue'
  var blackSquareGrey = 'lightskyblue'
  var $square = $('#board .square-' + square)

  var background = whiteSquareGrey
  if ($square.hasClass('black-3c85d')) {
    background = blackSquareGrey
  }

  $square.css('background', background)
}

function removeGreySquares () {
  $('#board .square-55d63').css('background', '')
}

function onMouseoverSquare (square, piece) {
  // get list of possible moves for this square
  var request = new XMLHttpRequest()
    request.open('GET', 'http://localhost:8080/chess/figure/move/possible/' + square, true)
    request.onload = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                var moves = JSON.parse(this.response)
                console.log(moves)
                
                // exit if there are no moves available for this square
                if (moves.length === 0) return

                // highlight the square they moused over
                greySquare(square)

                // highlight the possible squares for this piece
                for (var i = 0; i < moves.length; i++) {
                    greySquare(moves[i].x + moves[i].y)
                }

            }
            else {
                console.error(request.statusText)
            }
        }
    };
    request.send();
}

function onMouseoutSquare (square, piece) {
  removeGreySquares()
}

var config = {
    draggable: true,
    onDrop: onDrop,
    onMouseoverSquare: onMouseoverSquare,
    onMouseoutSquare: onMouseoutSquare
}

var board = Chessboard('board', config)

reload()