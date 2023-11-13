def print_chessboard_from_number(number):
    number=number.replace("L","")
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
            file = 7 - (i % 8)  # Corrected the file index for proper orientation
            chessboard[rank][file] = "X"

    # Print the chessboard with the correct orientation
    for rank in chessboard[::-1]:
        print(" ".join(rank))
    print("a b c d e f g h")

    return chessboard

# Example usage with a hexadecimal input that represents pawns on file H
print_chessboard_from_number("-281474708213761")  # This should print pawns on file H
