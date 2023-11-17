$(document).ready(function () {
    let computerColor = 'black'; // Default computer color

    const makeRequest = async (method, url, callback) => {
        try {
            const response = await fetch(url, { method });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const data = await response.json();
            if (callback) callback(data);
        } catch (error) {
            console.error('Request failed:', error);
        }
    };

    const updateCalculatedLine = () => {
        makeRequest('GET', 'http://localhost:8080/chess/line', (data) => {
            const lineText = data.map(item => item.move).join(", "); // Join the array elements with a comma and space
            const lastScore = data[data.length - 1].score; // Get the score from the last element
            document.getElementById('calculatedLine').innerText = `Calculated Line: ${lineText}`;
            document.getElementById("score").textContent = `SCORE: ${lastScore}`;
            //updateKingGlow(lastScore);
        });
    };
    

    function importFEN(fenString) {
        var encodedFenString = encodeURIComponent(fenString);
        makeRequest('PATCH', `http://localhost:8080/chess/fen?fen=${encodedFenString}`, () => {
            reloadBoard();
        });
    }

    function updateKingGlow(score) {
        const whiteKingElement = document.querySelector('[data-piece="wK"]');
        const blackKingElement = document.querySelector('[data-piece="bK"]');
    
        // Remove existing glow classes
        whiteKingElement.classList.remove('glow-red', 'glow-blue');
        blackKingElement.classList.remove('glow-red', 'glow-blue');
    
        // Apply new glow based on score
        if (score > 0) {
            whiteKingElement.classList.add('glow-blue');
        } else if (score < 0) {
            blackKingElement.classList.add('glow-red');
        }
    }
    


    document.getElementById('importFEN').addEventListener('click', function () {
        var fenString = prompt("Please enter FEN:");
        if (fenString) {
            importFEN(fenString);
        }
    });


    
    const checkState = (state) => {
        if (state !== "PLAY") {
            document.getElementById("header").textContent = state;
        }
    };

    const makeMove = (type, color) => {
        makeRequest('PATCH', `http://localhost:8080/chess/figure/move/${type}/${color}`, (data) => {
            checkState(data.state);
            reloadBoard();
        });
    };

    const onDrop = (source, target) => {
        makeRequest('PATCH', `http://localhost:8080/chess/figure/move/${source}/${target}`, reloadBoard);
    };

    const reloadBoard = () => {
        makeRequest('GET', 'http://localhost:8080/chess/figure/frontend', (data) => {
            board.position(data.renderBoard);
            updateCalculatedLine(); // Update the calculated line
        });
    };

    const highlightSquare = (square, highlight) => {
        const $square = $(`#board .square-${square}`);
        const background = $square.hasClass('black-3c85d') ? 'lightskyblue' : 'blue';
        $square.css('background', highlight ? background : '');
    };

    const onMouseoverSquare = (square) => {
        makeRequest('GET', `http://localhost:8080/chess/figure/move/possible/${square}`, (moves) => {
            if (moves.length === 0) return;
            highlightSquare(square, true);
            moves.forEach(move => highlightSquare(move.x + move.y, true));
        });
    };

    const onMouseoutSquare = () => {
        $('#board .square-55d63').css('background', '');
    };

    const initEventListeners = () => {
        $('#computerMove').on('click', () => {
            makeMove('intelligent', computerColor);
        });
        $('#resetBoard').on('click', () => {
            makeRequest('PUT', 'http://localhost:8080/chess/reset', () => {
                reloadBoard();
            });
        });
        $('#undoMove').on('click', () => {
            makeRequest('GET', 'http://localhost:8080/chess/undo', () => {
                reloadBoard();
            });
        });
        $('#autoPlay').on('click', () => {
            makeRequest('GET', 'http://localhost:8080/chess/autoplay', () => {
                reloadBoard();
            });
        });
    };

    const setBoardOrientation = (color) => {
        board.orientation(color); // Set board orientation to chosen color
    };

    const chooseColor = (color) => {
        setBoardOrientation(color); // Set orientation based on user choice
        computerColor = (color === 'white') ? 'black' : 'white'; // Computer plays opposite color
    };

    const initColorChoiceEventListeners = () => {
        $('#playWhite').on('click', () => chooseColor('white'));
        $('#playBlack').on('click', () => chooseColor('black'));
    };

    const boardConfig = {
        draggable: true,
        position: 'start',
        orientation: 'white', // Default orientation
        onDrop: onDrop,
        onMouseoverSquare: onMouseoverSquare,
        onMouseoutSquare: onMouseoutSquare
    };

    // Function to start the auto-refresh interval
    const startAutoRefresh = (intervalMs) => {
        setInterval(() => {
            reloadBoard();
        }, intervalMs);
    };

    const board = Chessboard('board', boardConfig);

    initEventListeners();
    initColorChoiceEventListeners();
    reloadBoard();
    startAutoRefresh(300);
});