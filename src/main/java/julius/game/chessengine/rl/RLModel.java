package julius.game.chessengine.rl;


import jakarta.annotation.PostConstruct;
import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameStateEnum;
import lombok.extern.log4j.Log4j2;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static julius.game.chessengine.board.MoveHelper.*;

@Log4j2
@Component
public class RLModel {

    public static String PATH_TO_MODEL = "RLModel.zip";

    private static final int inputSize = 768 + 221; // Bitboard encoding + Move encoding
    private static final int outputSize = 4096;


    private MultiLayerNetwork model;

    public RLModel() {
        this.model = createModel();
    }

    public void initUI() {
        try {
            UIServer uiServer = UIServer.getInstance();
            StatsStorage statsStorage = new InMemoryStatsStorage();
            int listenerFrequency = 1;
            model.setListeners(new StatsListener(statsStorage, listenerFrequency));
            uiServer.attach(statsStorage);
        } catch (Throwable e) {
            log.error("Error initializing UI server", e);
        }
    }

    private MultiLayerNetwork createModel() {
        var conf = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(150)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(150).nOut(100)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(100)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .build())
                .build();

        MultiLayerNetwork multiLayerNetwork = new MultiLayerNetwork(conf);
        multiLayerNetwork.init();
        return multiLayerNetwork;
    }



    public void train(Engine mainEngine) {
        Engine trainEngine = new Engine();
        double label = determineScoreForOutcome(mainEngine.getGameState().getState()); // This should be a continuous value now

        for (Integer move : mainEngine.getLine()) {
            try (INDArray input = encodeBoardStateAndMove(trainEngine.getBitBoard(), move)) {
                trainEngine.performMove(move);

                // Create a label INDArray with a single value and reshape it to [1, 1]
                INDArray labelArray = Nd4j.scalar(label).reshape(1, 1);

                // Reshape the input to be a 2D array with a single example
                INDArray reshapedInput = input.reshape(1, input.length());

                // Train the model
                model.setInput(reshapedInput);
                model.setLabels(labelArray);
                model.computeGradientAndScore();
                model.fit(reshapedInput, labelArray);
                log.info("updated model");
            }
            // Ensure proper cleanup of resources
        }
    }



    private double determineScoreForOutcome(GameStateEnum gameStateEnum) {
        // Return a continuous score based on the game outcome
        return switch (gameStateEnum) {
            case WHITE_WON -> 1.0; // Assign a high score for a white win
            case BLACK_WON -> -1.0; // Assign a negative score for a black win
            default -> 0.0; // Assign a neutral score for a draw or other outcomes
        };
    }


    public double predictMove(BitBoard bitBoard, int move) {
        // Encode the board state and the move
        INDArray encodedBoard = encodeBitboard(bitBoard);
        INDArray encodedMove = encodeMove(move);
        INDArray combinedInput = Nd4j.hstack(encodedBoard, encodedMove);

        // Reshape the input to be a 2D array with a single example (batch size = 1)
        INDArray reshapedInput = combinedInput.reshape(1, combinedInput.length());

        // Perform a forward pass to predict the move score
        INDArray output = model.output(reshapedInput);

        // Return the predicted score
        return output.getDouble(0);
    }

    public static INDArray encodeMove(int moveInt) {
        double[] encodedMove = new double[221];
        Arrays.fill(encodedMove, 0.0);

        int fromIndex = deriveFromIndex(moveInt);
        int toIndex = deriveToIndex(moveInt);

        encodedMove[fromIndex] = 1.0; // One-hot encoding for 'from' position
        encodedMove[64 + toIndex] = 1.0; // One-hot encoding for 'to' position

        // One-hot encoding for piece type
        int pieceTypeIndex = 128 + derivePieceTypeBits(moveInt);
        encodedMove[pieceTypeIndex] = 1.0;

        // One-hot encoding for captured piece type
        int capturedPieceTypeIndex = 136 + deriveCapturedPieceTypeBits(moveInt);
        encodedMove[capturedPieceTypeIndex] = 1.0;

        // One-hot encoding for promotion piece type
        int promotionPieceTypeIndex = 144 + derivePromotionPieceTypeBits(moveInt);
        encodedMove[promotionPieceTypeIndex] = 1.0;

        // Encoding special properties
        int specialPropertyIndex = 152;
        encodedMove[specialPropertyIndex] = isCapture(moveInt) ? 1.0 : 0.0;
        encodedMove[specialPropertyIndex + 1] = isCastlingMove(moveInt) ? 1.0 : 0.0;
        encodedMove[specialPropertyIndex + 2] = isEnPassantMove(moveInt) ? 1.0 : 0.0;
        encodedMove[specialPropertyIndex + 3] = isKingFirstMove(moveInt) ? 1.0 : 0.0;
        encodedMove[specialPropertyIndex + 4] = isRookFirstMove(moveInt) ? 1.0 : 0.0;

        // One-hot encoding for last double step pawn index
        int lastMoveDoubleStepPawnIndex = 157 + deriveLastMoveDoubleStepPawnIndex(moveInt);
        encodedMove[lastMoveDoubleStepPawnIndex] = 1.0;

        // Color of the piece making the move
        encodedMove[220] = isWhitesMove(moveInt) ? 1.0 : 0.0;

        return Nd4j.create(encodedMove);
    }

    public static INDArray encodeBitboard(BitBoard bitBoard) {
        double[] encodedBoard = new double[768]; // 12 piece types * 64 squares
        Arrays.fill(encodedBoard, 0.0);
        int index = 0;

        // Encoding for each type of piece
        index = fillArray(encodedBoard, index, bitBoard.getWhitePawns());
        index = fillArray(encodedBoard, index, bitBoard.getBlackPawns());
        index = fillArray(encodedBoard, index, bitBoard.getWhiteKnights());
        index = fillArray(encodedBoard, index, bitBoard.getBlackKnights());
        index = fillArray(encodedBoard, index, bitBoard.getWhiteBishops());
        index = fillArray(encodedBoard, index, bitBoard.getBlackBishops());
        index = fillArray(encodedBoard, index, bitBoard.getWhiteRooks());
        index = fillArray(encodedBoard, index, bitBoard.getBlackRooks());
        index = fillArray(encodedBoard, index, bitBoard.getWhiteQueens());
        index = fillArray(encodedBoard, index, bitBoard.getBlackQueens());
        index = fillArray(encodedBoard, index, bitBoard.getWhiteKing());
        index = fillArray(encodedBoard, index, bitBoard.getBlackKing());

        return Nd4j.create(encodedBoard);
    }

    private static int fillArray(double[] encodedBoard, int startIndex, long pieceBitboard) {
        for (int i = 0; i < 64; i++) {
            encodedBoard[startIndex + i] = (pieceBitboard >> i) & 1L;
        }
        return startIndex + 64;
    }


    public static INDArray encodeBoardStateAndMove(BitBoard bitBoard, int move) {
        // Encode the board state and the move
        INDArray encodedBoard = encodeBitboard(bitBoard);
        INDArray encodedMove = encodeMove(move);

        // Combine the encoded board state and move into a single array
        return Nd4j.hstack(encodedBoard, encodedMove);
    }

    public void saveModel() {
        try {
            ModelSerializer.writeModel(model, PATH_TO_MODEL, true);
            log.info("Saved Model");
        } catch (IOException e) {
            log.error("Error saving model");
        }
    }

    public void loadModel() {
        String filename = PATH_TO_MODEL;
        try {
            model = ModelSerializer.restoreMultiLayerNetwork(filename);
            initUI();
            log.info("Model loaded successfully from {}", filename);
        } catch (IOException e) {
            log.error("Error loading model from {}: {}", filename, e.getMessage());
            try {
                log.info("Creating a new model as fallback.");
                this.model = createModel();
                initUI();
                saveModel();
                log.info("New model created and saved successfully.");
            } catch (Exception ex) {
                log.error("Failed to create and save new model: {}", ex.getMessage());
                // You might want to handle this situation, e.g., by rethrowing the exception or taking other actions
            }
        }
    }

}