;(function () {

    var board = Chessboard('board', {
        draggable: true,
        dropOffBoard: 'trash'
    })

    // Create a request variable and assign a new XMLHttpRequest object to it.
    var request = new XMLHttpRequest()

    // Open a new connection, using the GET request on the URL endpoint
    request.open('GET', 'http://localhost:8080/chess/figure/frontend', true)
    request.setRequestHeader('Access-Control-Allow-Origin', '*');

    request.send();

    request.onload = function() {
        // Begin accessing JSON data here
        var data = JSON.parse(this.response)

        console.log(data.renderBoard)

        board.position(data.renderBoard)
    }
})() 