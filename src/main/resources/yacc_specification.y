E : T E`
    ;

E` : + T E`
    | ε
    ;

T : F T`
    ;

T` : * F T`
    | ε
    ;

F : ( E )
    | id
    ;