#ifndef HEAP_DEBUG_H_
#define HEAP_DEBUG_H_


#ifdef	__cplusplus
extern "C" {
#endif

#ifndef printf
#include <stdio.h>
#endif

//#define PRINTF printf
extern void Heap_Print(void *p_heap);

#ifdef	__cplusplus
}
#endif

#endif /* HEAP_DEBUG_H_ */
