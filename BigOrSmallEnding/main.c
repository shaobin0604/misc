#include <stdio.h>

static union { char c[4]; unsigned long l; } endian_test = { { 'l', '?', '?', 'b' } };
#define ENDIANNESS ((char)endian_test.l)

int main(int argc, char* argv[]) {
    printf("CPU ending is %c\n", ENDIANNESS);
    return 0;
}
