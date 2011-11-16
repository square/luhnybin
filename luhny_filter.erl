#!/usr/bin/env escript

% @author Alex Shneyderman <a.shneyderman@gmail.com>
% @copyright (C) 2011, Alex Shneyderman
% @since Nov 15, 2011
main(_Args) ->
    luhny_filter(io:get_line("")).

luhny_filter(Line) ->
    do_process_line(Line),
    luhny_filter(io:get_line("")).

do_process_line(Line) ->
    [_|RevLine] = lists:reverse( Line ),
    io:fwrite("~s~n",[lists:reverse( do_filter_line( RevLine ) )]).

do_filter_line( RevLine ) ->
    Line14 = do_filter_line( [], [], [], RevLine, 14 ),
    Line15 = do_filter_line( [], [], [], RevLine, 15 ),
    Line16 = do_filter_line( [], [], [], RevLine, 16 ),
    L0 = do_merge(Line14, RevLine),
    L1 = do_merge(Line15, L0),
    do_merge(Line16, L1).
do_merge( [CharM|TailM], [Char|Tail] ) when CharM =:= Char ->
   [Char] ++ do_merge(TailM,Tail); 
do_merge( [_CharM|TailM], [_Char|Tail] ) -> 
   "X" ++ do_merge(TailM,Tail); 
do_merge( [], [] ) ->
   [].

do_filter_line( Prefix, PotentialCC, DigitsOnly, [Digit|Tail], MatchLength ) when Digit > 47, 
                                                                                  Digit < 58, 
                                                                                  length(DigitsOnly) == (MatchLength - 1) ->
    
    case is_luhny_checked( DigitsOnly ++ [Digit] ) of
        true ->
            NextPotentialCC = replace_digits(PotentialCC ++ [Digit]),
            {NextAddToPrefix,NextCC} = split_on_first_symbol( NextPotentialCC ),
            [_FirstDigit|NextDigits] = DigitsOnly, 
            do_filter_line( Prefix ++ NextAddToPrefix, 
                            NextCC, 
                            NextDigits ++ [Digit], 
                            Tail,  
                            MatchLength );
        false -> 
            {AddToPrefix,NextCC} = split_on_first_symbol( PotentialCC ),
            [_FirstDigit|NextDigits] = DigitsOnly, 
            do_filter_line( Prefix ++ AddToPrefix, 
                            NextCC ++ [Digit], 
                            NextDigits ++ [Digit], 
                            Tail,  
                            MatchLength ) 
    end; 

do_filter_line( Prefix, PotentialCC, DigitsOnly, [Digit|Tail], MatchLength ) when Digit > 47, 
                                                                                  Digit < 58 ->
   do_filter_line( Prefix, PotentialCC ++ [Digit], DigitsOnly ++ [Digit], Tail,  MatchLength ); 

do_filter_line( Prefix, PotentialCC, DigitsOnly, [Space|Tail], MatchLength ) when Space =:= 32 ->
    do_filter_line( Prefix, PotentialCC ++ [Space], DigitsOnly, Tail, MatchLength);

do_filter_line( Prefix, PotentialCC, DigitsOnly, [Hyphen|Tail], MatchLength ) when Hyphen =:= 45 ->
    do_filter_line( Prefix, PotentialCC ++ [Hyphen], DigitsOnly, Tail, MatchLength);

do_filter_line( Prefix, PotentialCC, DigitsOnly, [Char|Tail], MatchLength ) ->
    case is_luhny_checked(DigitsOnly) of 
        true -> do_filter_line(Prefix ++ replace_digits(PotentialCC) ++ [Char], "", "", Tail, MatchLength);
        false -> do_filter_line(Prefix ++ PotentialCC ++ [Char], "", "",  Tail, MatchLength)
    end;

do_filter_line( Prefix, PotentialCC, DigitsOnly, [], _MatchLength ) ->
    case is_luhny_checked(DigitsOnly) of 
        true -> Prefix ++ replace_digits(PotentialCC);
        false -> Prefix ++ PotentialCC
    end.

is_luhny_checked( DigitsOnly ) when length(DigitsOnly) >= 14, length(DigitsOnly) =< 16 ->
    lists:foldl( fun( X, S ) -> sum_up_digits( integer_to_list(X) ) + S end, 
                 0, 
                 double_every_second_digit( DigitsOnly ) ) rem 10 =:= 0;
is_luhny_checked( _DigitsOnly ) ->
    false.

sum_up_digits(AsciiDigitsList) -> lists:foldl(fun(X,Sum) -> Sum + (X - 48) end, 0, AsciiDigitsList).

double_every_second_digit( DigitsOnly ) -> do_double_every_other( 1, DigitsOnly ).

do_double_every_other( 2, [DigitAscii|Tail] ) -> [(DigitAscii - 48) * 2] ++ do_double_every_other(1, Tail);
do_double_every_other( 1, [DigitAscii|Tail] ) -> [(DigitAscii - 48)] ++ do_double_every_other(2, Tail);                                           
do_double_every_other( _, [] ) -> [].

split_on_first_symbol( List ) -> split_on_first_symbol([],List).
split_on_first_symbol( Acc, [Digit|Tail] ) when Digit > 47, Digit < 58 -> { Acc ++ [Digit], Tail };
split_on_first_symbol( Acc, [X|Tail] ) when X == $X -> { Acc ++ [X], Tail };
split_on_first_symbol( Acc, [Char|Tail] ) ->  split_on_first_symbol( Acc ++ [Char], Tail ).

replace_digits([Digit|Tail]) when Digit > 47, Digit < 58 -> "X" ++ replace_digits(Tail);
replace_digits([Char|Tail]) -> [Char] ++ replace_digits(Tail);
replace_digits([]) -> [].
