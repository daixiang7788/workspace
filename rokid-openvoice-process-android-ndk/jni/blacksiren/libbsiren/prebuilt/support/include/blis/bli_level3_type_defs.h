/*

   BLIS    
   An object-based framework for developing high-performance BLAS-like
   libraries.

   Copyright (C) 2014, The University of Texas at Austin

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    - Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    - Neither the name of The University of Texas at Austin nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

#ifndef BLIS_LEVEL3_TYPE_DEFS_H
#define BLIS_LEVEL3_TYPE_DEFS_H


//
// -- BLIS level-3 operation types ---------------------------------------------
//

// Here we generate typedef statements that generate function pointer types
// for all defined level-3 operations.



// -- gemm --

#undef  GENTDEF
#define GENTDEF( opname ) \
\
typedef void \
(*PASTECH(opname,_fp_t))( \
                          obj_t*  alpha, \
                          obj_t*  a, \
                          obj_t*  b, \
                          obj_t*  beta, \
                          obj_t*  c  \
                        );
GENTDEF( gemm )
GENTDEF( her2k )
GENTDEF( syr2k )


// -- hemm/symm/trmm3 --

#undef  GENTDEF
#define GENTDEF( opname ) \
\
typedef void \
(*PASTECH(opname,_fp_t))( \
                          side_t  side, \
                          obj_t*  alpha, \
                          obj_t*  a, \
                          obj_t*  b, \
                          obj_t*  beta, \
                          obj_t*  c  \
                        );
GENTDEF( hemm )
GENTDEF( symm )
GENTDEF( trmm3 )


// -- herk/syrk --

#undef  GENTDEF
#define GENTDEF( opname ) \
\
typedef void \
(*PASTECH(opname,_fp_t))( \
                          obj_t*  alpha, \
                          obj_t*  a, \
                          obj_t*  beta, \
                          obj_t*  c  \
                        );
GENTDEF( herk )
GENTDEF( syrk )


// -- trmm/trsm --

#undef  GENTDEF
#define GENTDEF( opname ) \
\
typedef void \
(*PASTECH(opname,_fp_t))( \
                          side_t  side, \
                          obj_t*  alpha, \
                          obj_t*  a, \
                          obj_t*  b  \
                        );
GENTDEF( trmm )
GENTDEF( trsm )



#endif

