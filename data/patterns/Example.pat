# Lines starting with a # are comments
#
# Cell formatting:
#
# O or 0: an empty (dead) cell (o or the number both work fine)
# X: a filled (alive) cell
# B: blocked cell (rock)
# S: spawning cell (always stays alive)
# All other characters are considered to be comments and are ignored 
#

# Then, specify the level, row by row
# The example pattern shows the hexagon
# Shared cells are set as alive cells, the center point is set as 
# a spawner

             X     X     X
          O     O     O     O
       X     O     O     O     X
          O     O     O     O
       O     O     O     O     O
    X     O     O     O     O     X
       O     O     O     O     O
    O     O     O     O     O     O
 X     O     O     O     O     O     X
    O     O     O     O     O     O
 O     O     O     S     O     O     O
    O     O     O     O     O     O
 X     O     O     O     O     O     X
    O     O     O     O     O     O
       O     O     O     O     O
    X     O     O     O     O     X
       O     O     O     O     O
          O     O     O     O
       X     O     O     O     X
          O     O     O     O
             X     X     X

