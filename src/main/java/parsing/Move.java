package parsing;

import simulation.*;

import java.util.Map;

import static java.util.Map.entry;

public class Move {
    private char piece;
    private final Color color;
    private final String action;
    private Position newPosition;
    private char file;
    private int rank;
    private final String comment;
    private boolean capture;
    private boolean check;
    private boolean checkmate;
    private final String annotation;
    private boolean promotion;
    private boolean kingSideCastling;
    private boolean queenSideCastling;
    private char promoted;
    private boolean isCharAmb;
    private boolean isDigitAmb;

    public boolean isKingSideCastling() {
        return kingSideCastling;
    }

    public boolean isQueenSideCastling() {
        return queenSideCastling;
    }

    Map<Integer, String> annotations = Map.<Integer, String>ofEntries(
            entry(0, "null annotation"),
            entry(1, "good move (traditional \"!\")"),
            entry(2, "poor move or mistake (traditional \"?\")"),
            entry(3, "very good or brilliant move (traditional \"!!\")"),
            entry(4, "very poor move or blunder (traditional \"??\")"),
            entry(5, "speculative or interesting move (traditional \"!?\")"),
            entry(6, "questionable or dubious move (traditional \"?!\")"),
            entry(7, "forced move (all others lose quickly) or only move"),
            entry(8, "singular move (no reasonable alternatives)"),
            entry(9, "worst move"),
            entry(10, "drawish position or even"),
            entry(11, "equal chances, quiet position"),
            entry(12, "equal chances, active position"),
            entry(13, "unclear position"),
            entry(14, "White has a slight advantage"),
            entry(15, "Black has a slight advantage"),
            entry(16, "White has a moderate advantage"),
            entry(17, "Black has a moderate advantage"),
            entry(18, "White has a decisive advantage"),
            entry(19, "Black has a decisive advantage"),
            entry(20, "White has a crushing advantage (Black should resign)"),
            entry(21, "Black has a crushing advantage (White should resign)"),
            entry(22, "White is in zugzwang"),
            entry(23, "Black is in zugzwang"),
            entry(24, "White has a slight space advantage"),
            entry(25, "Black has a slight space advantage"),
            entry(26, "White has a moderate space advantage"),
            entry(27, "Black has a moderate space advantage"),
            entry(28, "White has a decisive space advantage"),
            entry(29, "Black has a decisive space advantage"),
            entry(30, "White has a slight time (development) advantage"),
            entry(31, "Black has a slight time (development) advantage"),
            entry(32, "White has a moderate time (development) advantage"),
            entry(33, "Black has a moderate time (development) advantage"),
            entry(34, "White has a decisive time (development) advantage"),
            entry(35, "Black has a decisive time (development) advantage"),
            entry(36, "White has the initiative"),
            entry(37, "Black has the initiative"),
            entry(38, "White has a lasting initiative"),
            entry(39, "Black has a lasting initiative"),
            entry(40, "White has the attack"),
            entry(41, "Black has the attack"),
            entry(42, "White has insufficient compensation for material deficit"),
            entry(43, "Black has insufficient compensation for material deficit"),
            entry(44, "White has sufficient compensation for material deficit"),
            entry(45, "Black has sufficient compensation for material deficit"),
            entry(46, "White has more than adequate compensation for material deficit"),
            entry(47, "Black has more than adequate compensation for material deficit"),
            entry(48, "White has a slight center control advantage"),
            entry(49, "Black has a slight center control advantage"),
            entry(50, "White has a moderate center control advantage"),
            entry(51, "Black has a moderate center control advantage"),
            entry(52, "White has a decisive center control advantage"),
            entry(53, "Black has a decisive center control advantage"),
            entry(54, "White has a slight kingside control advantage"),
            entry(55, "Black has a slight kingside control advantage"),
            entry(56, "White has a moderate kingside control advantage"),
            entry(57, "Black has a moderate kingside control advantage"),
            entry(58, "White has a decisive kingside control advantage"),
            entry(59, "Black has a decisive kingside control advantage"),
            entry(60, "White has a slight queenside control advantage"),
            entry(61, "Black has a slight queenside control advantage"),
            entry(62, "White has a moderate queenside control advantage"),
            entry(63, "Black has a moderate queenside control advantage"),
            entry(64, "White has a decisive queenside control advantage"),
            entry(65, "Black has a decisive queenside control advantage"),
            entry(66, "White has a vulnerable first rank"),
            entry(67, "Black has a vulnerable first rank"),
            entry(68, "White has a well protected first rank"),
            entry(69, "Black has a well protected first rank"),
            entry(70, "White has a poorly protected king"),
            entry(71, "Black has a poorly protected king"),
            entry(72, "White has a well protected king"),
            entry(73, "Black has a well protected king"),
            entry(74, "White has a poorly placed king"),
            entry(75, "Black has a poorly placed king"),
            entry(76, "White has a well placed king"),
            entry(77, "Black has a well placed king"),
            entry(78, "White has a very weak pawn structure"),
            entry(79, "Black has a very weak pawn structure"),
            entry(80, "White has a moderately weak pawn structure"),
            entry(81, "Black has a moderately weak pawn structure"),
            entry(82, "White has a moderately strong pawn structure"),
            entry(83, "Black has a moderately strong pawn structure"),
            entry(84, "White has a very strong pawn structure"),
            entry(85, "Black has a very strong pawn structure"),
            entry(86, "White has poor knight placement"),
            entry(87, "Black has poor knight placement"),
            entry(88, "White has good knight placement"),
            entry(89, "Black has good knight placement"),
            entry(90, "White has poor bishop placement"),
            entry(91, "Black has poor bishop placement"),
            entry(92, "White has good bishop placement"),
            entry(93, "Black has good bishop placement"),
            entry(94, "White has poor rook placement"),
            entry(95, "Black has poor rook placement"),
            entry(96, "White has good rook placement"),
            entry(97, "Black has good rook placement"),
            entry(98, "White has poor queen placement"),
            entry(99, "Black has poor queen placement"),
            entry(100, "White has good queen placement"),
            entry(101, "Black has good queen placement"),
            entry(102, "White has poor piece coordination"),
            entry(103, "Black has poor piece coordination"),
            entry(104, "White has good piece coordination"),
            entry(105, "Black has good piece coordination"),
            entry(106, "White has played the opening very poorly"),
            entry(107, "Black has played the opening very poorly"),
            entry(108, "White has played the opening poorly"),
            entry(109, "Black has played the opening poorly"),
            entry(110, "White has played the opening well"),
            entry(111, "Black has played the opening well"),
            entry(112, "White has played the opening very well"),
            entry(113, "Black has played the opening very well"),
            entry(114, "White has played the middlegame very poorly"),
            entry(115, "Black has played the middlegame very poorly"),
            entry(116, "White has played the middlegame poorly"),
            entry(117, "Black has played the middlegame poorly"),
            entry(118, "White has played the middlegame well"),
            entry(119, "Black has played the middlegame well"),
            entry(120, "White has played the middlegame very well"),
            entry(121, "Black has played the middlegame very well"),
            entry(122, "White has played the ending very poorly"),
            entry(123, "Black has played the ending very poorly"),
            entry(124, "White has played the ending poorly"),
            entry(125, "Black has played the ending poorly"),
            entry(126, "White has played the ending well"),
            entry(127, "Black has played the ending well"),
            entry(128, "White has played the ending very well"),
            entry(129, "Black has played the ending very well"),
            entry(130, "White has slight counterplay"),
            entry(131, "Black has slight counterplay"),
            entry(132, "White has moderate counterplay"),
            entry(133, "Black has moderate counterplay"),
            entry(134, "White has decisive counterplay"),
            entry(135, "Black has decisive counterplay"),
            entry(136, "White has moderate time control pressure"),
            entry(137, "Black has moderate time control pressure"),
            entry(138, "White has severe time control pressure / zeitnot"),
            entry(139, "Black has severe time control pressure / zeitnot")
    );

    public Move(String action, String comment, String annotation, Color color){
        this.action = action;
        this.comment = comment;
        this.annotation = annotation;
        this.color = color;
        this.isCharAmb = false; // Explicitly initialize to false
        this.isDigitAmb = false; // Explicitly initialize to false

        if(annotation != null && annotation.contains("$")){
            int annotationKey = 0;
            int i = annotation.indexOf("$")+1;
            while (i < annotation.length() && Character.isDigit(annotation.charAt(i))){
                annotationKey = annotationKey*10 + Character.getNumericValue(annotation.charAt(i));
                i++;
            }
            if(annotations.containsKey(annotationKey)){
                System.out.println(action+ " "+ annotations.get(annotationKey));
            }
            else {
                System.out.println("Invalid annotation, value not defined");
            }

        }


        // checkmate
        if(action.contains("#"))
            checkmate = true;
            // check
        else if(action.contains("+"))
            check = true;

        if(action.equals("O-O")){
            kingSideCastling = true;
            return;
        }
        if(action.equals("O-O-O")){
            queenSideCastling = true;
            return;
        }

        char firstChar = action.charAt(0);
        // piece is pawn
        if(Character.isLowerCase(firstChar)){
            piece = 'P';

            // In the Move constructor where pawn captures are handled:
            // capturing
            if(action.contains("x")) {
                capture = true;
                char originFile = action.charAt(0);
                char targetFile = action.charAt(action.indexOf('x')+1);

                // For pawn captures, the file character before 'x' is always the source file
                // This is not ambiguity in the traditional sense, but the way captures are notated
                file = originFile;  // Store the origin file

                int y = Character.getNumericValue(action.charAt(action.indexOf('x')+2));
                newPosition = new Position(targetFile, y);

                // For pawn captures, file disambiguation is a special case
                // It's always needed as part of the notation, but it's not due to multiple pieces
                // being able to make the same move
                isCharAmb = true;
            }
            // promotion
            if(action.contains("=")) {
                promotion = true;
                promoted = action.charAt(action.indexOf("=")+1);

                // Handle the position
                char x = action.charAt(0);
                int y = Character.getNumericValue(action.charAt(1));
                newPosition = new Position(x, y);

                // For simple pawn moves, no file disambiguation is needed
                isCharAmb = false;
            }
            // ordinary move
            else if(Character.isDigit(action.charAt(1))){
                char x = action.charAt(0);
                int y = Character.getNumericValue(action.charAt(1));
                newPosition = new Position(x, y);

                // For regular pawn moves, there's no file ambiguity
                // The file character is part of the destination, not disambiguation
                isCharAmb = false;
            }
            return;
        }

        this.piece = action.charAt(0);


        // Inside the Move class, update the section that handles knights and other pieces:

        // Non-pawn, non-king pieces (like knights)
        if (Character.isUpperCase(firstChar) && firstChar != 'K') {
            this.piece = firstChar;

            if (action.contains("x")) {
                // Handle captures with disambiguation
                // (existing code)
            } else {
                int yPosition;
                int xPosition;

                // Check for file disambiguation (like Nhf6)
                if (action.length() >= 4 && Character.isLetter(action.charAt(1)) &&
                        Character.isLetter(action.charAt(2)) && Character.isDigit(action.charAt(3))) {

                    // This is a file disambiguation (e.g., "Nhf6")
                    isCharAmb = true;
                    isDigitAmb = false;
                    file = action.charAt(1);
                    yPosition = 2;
                    xPosition = 3;
                }
                // Check for rank disambiguation (like N6d7)
                else if (action.length() >= 4 && Character.isDigit(action.charAt(1)) &&
                        Character.isLetter(action.charAt(2)) && Character.isDigit(action.charAt(3))) {

                    // This is a rank disambiguation (e.g., "N6d7")
                    isDigitAmb = true;
                    isCharAmb = false;
                    rank = Character.getNumericValue(action.charAt(1));
                    yPosition = 2;
                    xPosition = 3;
                }
                // Check for both file and rank disambiguation (like Nf6d7)
                else if (action.length() >= 5 && Character.isLetter(action.charAt(1)) &&
                        Character.isDigit(action.charAt(2)) && Character.isLetter(action.charAt(3)) &&
                        Character.isDigit(action.charAt(4))) {

                    // This has both file and rank disambiguation
                    isCharAmb = true;
                    isDigitAmb = true;
                    file = action.charAt(1);
                    rank = Character.getNumericValue(action.charAt(2));
                    yPosition = 3;
                    xPosition = 4;
                }
                // No disambiguation (like Nd7)
                else {
                    isCharAmb = false;
                    isDigitAmb = false;
                    yPosition = 1;
                    xPosition = 2;
                }

                char x = action.charAt(yPosition);
                int y = Character.getNumericValue(action.charAt(xPosition));
                this.newPosition = new Position(x, y);
            }
        }

        if(action.contains("x")){
            capture = true;
            int yPosition;
            int xPosition;

            // capture with no ambiguity
            if(action.indexOf("x") == 1){
                yPosition = 2;
                xPosition = 3;
            }
            else if(action.indexOf("x") == 2){
                // capture with digit ambiguity
                if(Character.isDigit(action.charAt(1))){
                    isDigitAmb = true;
                    rank = Character.getNumericValue(action.charAt(1));
                }
                // capture with char ambiguity
                else {
                    isCharAmb = true;
                    file = action.charAt(1);
                }
                yPosition = 3;
                xPosition = 4;
            }
            // capture with char and digit ambiguity
            else{
                isCharAmb = true;
                isDigitAmb = true;
                file = action.charAt(1);
                rank = Character.getNumericValue(action.charAt(2));
                yPosition = 4;
                xPosition = 5;
            }
            char x = action.charAt(yPosition);
            int y = Character.getNumericValue(action.charAt(xPosition));
            newPosition = new Position(x,y);
        }
        else {
            int yPosition;
            int xPosition;
            // no ambiguity
            if((action.length() == 3 || action.length() == 4) && Character.isAlphabetic(action.charAt(1)) && Character.isDigit(action.charAt(2))){
                yPosition = 1;
                xPosition = 2;
            }
            else if(Character.isAlphabetic(action.charAt(2)) && Character.isDigit(action.charAt(3))){
                // ambiguity with only char
                if(Character.isAlphabetic(action.charAt(1))){
                    isCharAmb = true;
                    this.file = action.charAt(1);
                }
                // ambiguity with only digit
                else {
                    isDigitAmb = true;
                    this.rank = Character.getNumericValue(action.charAt(1));
                }
                yPosition = 2;
                xPosition = 3;
            }
            // ambiguity with char and digit
            else {
                yPosition = 3;
                xPosition = 4;
                isCharAmb = true;
                isDigitAmb = true;
                file = action.charAt(1);
                rank = Character.getNumericValue(action.charAt(2));
            }
            char x = action.charAt(yPosition);
            int y = Character.getNumericValue(action.charAt(xPosition));
            this.newPosition = new simulation.Position(x,y);
        }
    }

    public boolean isCharAmb(){
        return isCharAmb;
    }

    public boolean isDigitAmb(){
        return isDigitAmb;
    }

    public Color getColor() { return color; }

    public char getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    public boolean isCapture() {
        return capture;
    }

    public boolean isCheck() {
        return check;
    }

    public boolean isCheckmate() {
        return checkmate;
    }

    public boolean isPromotion() {
        return promotion;
    }

    public char getPiece() {
        return piece;
    }

    public Position getNewPosition() {
        return newPosition;
    }

    public String getComment() {
        return comment;
    }

    public String getAction() {
        return action;
    }

    public char getPromoted() {
        return promoted;
    }

    public String getAnnotation() {
        return annotation;
    }

    @Override
    public String toString() {
        return action;
    }
}