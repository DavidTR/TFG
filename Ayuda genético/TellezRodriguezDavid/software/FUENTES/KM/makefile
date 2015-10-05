CFLAGS = -g -c

C = k-medias.c input.c util-km.c

O = k-medias.o input.o util-km.o

.c.o:
	gcc $(CFLAGS) $(C)

k-medias: k-medias.o $(O)
	gcc -o k-medias $(O) -lm
