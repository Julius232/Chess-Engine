def print_chessboard_from_number(number):

    number = number.replace("L", "")
    # Determine the input format (hex, binary, or decimal)
    if isinstance(number, str):
        if number.startswith("0x"):
            # Hexadecimal
            number = int(number, 16)
        elif number.startswith("0b"):
            # Binary
            number = int(number, 2)
        else:
            # Decimal string
            number = int(number)
    elif isinstance(number, int):
        # Decimal number
        pass
    else:
        return "Invalid input format. Please enter a decimal, hexadecimal, or binary number."

    # Convert the number to a binary string and fill with leading zeros to represent all squares
    binary_string = bin(number & 0xFFFFFFFFFFFFFFFF)[2:].zfill(64)

    # Initialize the chessboard as a list of lists with empty squares
    chessboard = [["." for _ in range(8)] for _ in range(8)]

    # Fill the chessboard with pieces (P for demonstration) where the binary string has a 1
    for i, bit in enumerate(binary_string):
        if bit == "1":
            rank = 7 - (i // 8)
            # Corrected the file index for proper orientation
            file = 7 - (i % 8)
            chessboard[rank][file] = "X"

    # Print the chessboard with the correct orientation
    for rank in chessboard[::-1]:
        print(" ".join(rank))
    print("a b c d e f g h")

    return chessboard


def orderMagicNumbers(data):
    # Parsing the data into a dictionary
    data_dict = {}
    for line in data.strip().split('\n'):
        key, value = line.split(':')
        data_dict[int(key)] = int(value)

    # Sorting the dictionary by key
    sorted_data = sorted(data_dict.items())

    # Output the sorted data
    sorted_data_str = "\n".join(
        [f"{key}:{value}" for key, value in sorted_data])
    print(sorted_data_str)


# Example usage with a hexadecimal input that represents pawns on file H
# This should print pawns on file H
print_chessboard_from_number("292728875076")

bishopData = """
    62:2342056532843430052
    60:70506730491968
    39:3500423360264544520
    0:-9149053842547896303
    63:-9149053842547896303
    6:-9222241708818821043
    4:3382235409548552
    3:1139120088809744
    17:513410366144284769
    5:282579052404802
    15:4760305222876794881
    24:-8070133853303303919
    31:1235114534441616388
    50:2616872867606888456
    61:132216290033920
    52:6341640214708224034
    47:290482730561437772
    30:216244250369851436
    14:198161716503380032
    51:7164207038984
    53:2286988508006402
    49:2343280283043889412
    1:2295784580186132
    10:1152945696011224192
    59:1152945696011224192
    41:292813145003528196
    16:182403619466018947
    57:18089448776441856
    7:18647863252787328
    25:1729946314999071242
    8:5764634048903258149
    23:-9078693863385718520
    58:2613223579526924288
    2:22536141422034949
    13:-9214362638037806078
    55:2378199676999467035
    22:585643874493401632
    12:-9043223653175909373
    9:-6620273859478155231
    48:2522165337928302594
    33:-9215490599931215743
    32:18665584543139841
    40:2461221637373042708
    38:72094294534291465
    46:1162074939587559736
    56:648591000150872064
    54:-4611615372641038335
    37:18295890682871840
    11:1441223641080529952
    34:36037601704099844
    42:-9223077298951929852
    20:144119732152304128
    29:1297108161107329104
    21:144255925698445316
    19:144123992767202564
    28:-8926133774232582080
    18:74309445393326113
    43:306842926168672274
    26:1229487097132353541
    45:4902185789263708673
    44:6378224075029873666
    35:4503737603719177
    27:2341889400582965249
    36:2305930978734120965
    """
#orderMagicNumbers(bishopData)