E : E addorminus T
    | T
    ;

T : T multiply F
    | F
    ;

F : leftParentheses E rightParentheses
    | id
    ;