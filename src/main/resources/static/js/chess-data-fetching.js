let latestGameData = null;

// Function to make API requests
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

// Update the calculated line and game details
const updateCalculatedLine = () => {
    makeRequest('GET', 'http://localhost:8080/chess/state', (data) => {
        latestGameData = data;    
        const lineText = data.move || "No moves yet";
        const gameState = data.gameState.state;
        const score = data.score;

        document.getElementById('calculatedLine').innerText = `Calculated Line: ${lineText}`;
        document.getElementById('score').textContent = `SCORE: ${score}`;

        checkState(gameState);
        updateKingGlow(gameState); // Update the king glow based on game state
        updateGameDetails(data);
    });
};

// Update the details in the modal
const updateGameDetails = (data) => {
    let details = '<div class="game-details-container">';

    details += '<table class="details-table">';
    details += '<tr><td>Game State</td><td>' + data.gameState.state + '</td></tr>';
    details += '<tr><td>Overall Score</td><td>' + data.score + '</td></tr>';
    details += '<tr><td>White Score</td><td>' + data.gameState.score.whiteScore + '</td></tr>';
    details += '<tr><td>Black Score</td><td>' + data.gameState.score.blackScore + '</td></tr>';

    // Piece Scores
    details += '<tr><td>White Pawns</td><td>' + data.gameState.score.whitePawns + '</td></tr>';
    details += '<tr><td>Black Pawns</td><td>' + data.gameState.score.blackPawns + '</td></tr>';
    details += '<tr><td>White Knights</td><td>' + data.gameState.score.whiteKnights + '</td></tr>';
    details += '<tr><td>Black Knights</td><td>' + data.gameState.score.blackKnights + '</td></tr>';
    details += '<tr><td>White Bishops</td><td>' + data.gameState.score.whiteBishops + '</td></tr>';
    details += '<tr><td>Black Bishops</td><td>' + data.gameState.score.blackBishops + '</td></tr>';
    details += '<tr><td>White Rooks</td><td>' + data.gameState.score.whiteRooks + '</td></tr>';
    details += '<tr><td>Black Rooks</td><td>' + data.gameState.score.blackRooks + '</td></tr>';
    details += '<tr><td>White Queens</td><td>' + data.gameState.score.whiteQueens + '</td></tr>';
    details += '<tr><td>Black Queens</td><td>' + data.gameState.score.blackQueens + '</td></tr>';

    // Misc Details
    details += '<tr><td>Agility White</td><td>' + data.gameState.score.agilityWhite + '</td></tr>';
    details += '<tr><td>Agility Black</td><td>' + data.gameState.score.agilityBlack + '</td></tr>';
    details += '<tr><td>Score Difference</td><td>' + data.gameState.score.scoreDifference + '</td></tr>';
    details += '<tr><td>Game Over</td><td>' + data.gameState.gameOver + '</td></tr>';
    details += '<tr><td>In State Check</td><td>' + data.gameState.inStateCheck + '</td></tr>';
    details += '<tr><td>In State CheckMate</td><td>' + data.gameState.inStateCheckMate + '</td></tr>';
    details += '<tr><td>In State Draw</td><td>' + data.gameState.inStateDraw + '</td></tr>';

    // Repetition Counter
    details += '<tr><td colspan="2"><strong>Repetition Counter</strong></td></tr>';
    for (const hash in data.gameState.repetitionCounter) {
        details += `<tr><td colspan="2">${hash}: ${data.gameState.repetitionCounter[hash]}</td></tr>`;
    }

    details += '</table>';
    details += '</div>';

    document.getElementById('gameDetails').innerHTML = details;
};



// Modal handling code
const setupModal = () => {
    const viewDetails = document.getElementById("viewDetails");
    const modal = document.getElementById("detailsModal");
    const closeModal = document.querySelector('.close');

    if (viewDetails && modal && closeModal) {
        viewDetails.onclick = function () {
            modal.style.display = "block";
            updateGameDetails(latestGameData); // Update the modal with the latest game data
        }

        closeModal.onclick = function () {
            modal.style.display = "none";
        }

        window.onclick = function (event) {
            if (event.target == modal) {
                modal.style.display = "none";
            }
        }
    } else {
        console.error('Modal or associated elements not found in the document.');
    }
};

// Function to update the king glow
function updateKingGlow(gameState) {
    const whiteKingElement = document.querySelector('[data-piece="wK"]');
    const blackKingElement = document.querySelector('[data-piece="bK"]');

    // Remove existing glow classes
    whiteKingElement.classList.remove('glow-red', 'glow-blue');
    blackKingElement.classList.remove('glow-red', 'glow-blue');

    // Apply glow based on the game state
    switch (gameState) {
        case 'WHITE_IN_CHECK':
            whiteKingElement.classList.add('glow-blue');
            break;
        case 'BLACK_IN_CHECK':
            blackKingElement.classList.add('glow-red');
            break;
        case 'WHITE_WON':
            blackKingElement.classList.add('glow-blue'); // Blue glow to indicate loss
            break;
        case 'BLACK_WON':
            whiteKingElement.classList.add('glow-red'); // Blue glow to indicate loss
            break;
        // No glow applied for PLAY and DRAW states
    }
}

const checkState = (state) => {
    if (state !== "PLAY") {
        document.getElementById("header").textContent = state;
    }
};

// Function to import FEN
function importFEN(fenString) {
    var encodedFenString = encodeURIComponent(fenString);
    makeRequest('PATCH', `http://localhost:8080/chess/fen?fen=${encodedFenString}`, () => {
        reloadBoard();
    });
}

// Function to make a move
const makeMove = (type, color) => {
    makeRequest('PATCH', `http://localhost:8080/chess/figure/move/${type}/${color}`, (data) => {
        checkState(data.state);
        reloadBoard();
    });
};
// Function for onDrop event
const onDrop = (source, target) => {
    makeRequest('PATCH', `http://localhost:8080/chess/figure/move/${source}/${target}`, reloadBoard);
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

const highlightSquare = (square, highlight) => {
    const $square = $(`#board .square-${square}`);
    const background = $square.hasClass('black-3c85d') ? 'lightskyblue' : 'blue';
    $square.css('background', highlight ? background : '');
};

let board; // Declare the board object at a scope accessible by all functions

const initBoard = () => {
    const boardConfig = {
        draggable: true,
        position: 'start',
        orientation: 'white', // Default orientation
        onDrop: onDrop, // Ensure this function is defined in chess-data-fetching.js or imported from another script
        onMouseoverSquare: onMouseoverSquare, // Ensure this function is defined in chess-data-fetching.js or imported from another script
        onMouseoutSquare: onMouseoutSquare // Ensure this function is defined in chess-data-fetching.js or imported from another script
    };

    board = Chessboard('board', boardConfig); // Initialize the board
};

const reloadBoard = () => {
    makeRequest('GET', 'http://localhost:8080/chess/figure/frontend', (data) => {
        if (board) {
            board.position(data.renderBoard);
            updateCalculatedLine(); // Update the calculated line
        } else {
            console.error('Board not initialized');
        }
    });
};

// Function to start the auto-refresh interval
const startAutoRefresh = (intervalMs) => {
    setInterval(() => {
        reloadBoard();
    }, intervalMs);
};

$(document).ready(function () {
    initBoard(); // Initialize the board when the document is ready
    startAutoRefresh(300); // Start auto-refresh
});