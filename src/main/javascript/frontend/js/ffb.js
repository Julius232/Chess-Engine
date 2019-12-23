function makeRandomMove() {
    var request = new XMLHttpRequest()
    request.open('PATCH', 'http://localhost:8080/chess/figure/move/random/black', true)
    request.send();
    request.onload = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                reload();
            }
            else {
                console.error(request.statusText)
            }
        }
    };
}

function onDrop (source, target, piece, newPos, oldPos, orientation) {

    var request = new XMLHttpRequest()

    // Open a new connection, using the GET request on the URL endpoint
    request.open('PATCH', 'http://localhost:8080/chess/figure/move/' + source + '/' + target, true)
    request.send();
    request.onload = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                console.log('Source: ' + source)
                console.log('Target: ' + target)
                console.log('~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~')
                reload();
            }
            else {
                console.error(request.statusText)
            }
        }
    };
    window.setTimeout(makeRandomMove, 250)
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
    request.send();
    request.onload = function(e) {
        if (request.readyState == 4) {
            if (request.status == 200) {
                var data = JSON.parse(this.response)
                console.log(data.renderBoard)
                board.position(data.renderBoard)
            }
            else {
                console.error(request.statusText)
            }
        }
    };
}

function greySquare (square) {

  var whiteSquareGrey = '#A9F5A9'
  var blackSquareGrey = '#04B404'
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
    request.send();
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
                    greySquare(moves[i].xachse + moves[i].yachse)
                }

            }
            else {
                console.error(request.statusText)
            }
        }
    };
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