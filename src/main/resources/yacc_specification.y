S : A B
    | testC
    ;

A : testA
    | epsilon
    ;

B : testB S
    | epsilon
    ;