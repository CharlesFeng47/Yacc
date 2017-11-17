E : E add T
    | T
    ;

T : T multiply F
    | F
    ;

F : ( E )
    | id
    ;