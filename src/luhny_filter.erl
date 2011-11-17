% @author Alex Shneyderman <a.shneyderman@gmail.com>
% @copyright (C) 2011, Alex Shneyderman
% @since Nov 15, 2011
-module(luhny_filter).

-export([start/0,process_line/1,do_within_process/1]).

start() -> process_line(io:get_line("")).

process_line(Line) ->
    do_process_line(Line),
    process_line(io:get_line("")).

do_process_line(Line) ->
    [_|RevLine] = lists:reverse(Line),
    io:fwrite("~s~n",[lists:reverse(do_filter_line(RevLine))]).

do_filter_line(RevLine) ->
    Pid14 = spawn(luhny_filter, do_within_process, [self()]),
    Pid15 = spawn(luhny_filter, do_within_process, [self()]),
    Pid16 = spawn(luhny_filter, do_within_process, [self()]),
    
    Pid14 ! { RevLine, 14 },
    Pid15 ! { RevLine, 15 },
    Pid16 ! { RevLine, 16 },
    
    MaskedLines = do_wait(Pid14, Pid15, Pid16, []),
    lists:foldl(fun(MLine, FLine) -> 
                    do_merge(MLine,FLine) 
                end, RevLine, MaskedLines).

do_within_process(ResponsePid) ->
    receive
        { RevLine, MatchLength } -> ResponsePid ! { self(), do_filter_line([], [], [], RevLine, MatchLength) };
        _ -> do_within_process(ResponsePid)
    end.
    
do_wait(_Pid14, _Pid15, _Pid16, CurrentLines) when is_list(CurrentLines), length(CurrentLines) =:= 3 ->
     CurrentLines;
do_wait(Pid14, Pid15, Pid16, CurrentLines) ->
    receive
        { Pid14, Line14 } -> do_wait(Pid14, Pid15, Pid16, CurrentLines ++ [Line14]);
        { Pid15, Line15 } -> do_wait(Pid14, Pid15, Pid16, CurrentLines ++ [Line15]);
        { Pid16, Line16 } -> do_wait(Pid14, Pid15, Pid16, CurrentLines ++ [Line16]) 
    end.

do_filter_line(Prefix, PotentialCC, DigitsOnly, [Digit|Tail], MatchLength) when Digit >= $0, Digit =< $9, length(DigitsOnly) == (MatchLength - 1) ->
    case is_luhny_checked(DigitsOnly ++ [Digit]) of
        true ->
            NextPotentialCC = replace_digits(PotentialCC ++ [Digit]),
            {NextAddToPrefix,NextCC} = split_on_first_symbol(NextPotentialCC),
            [_FirstDigit|NextDigits] = DigitsOnly, 
            do_filter_line(Prefix ++ NextAddToPrefix, 
                           NextCC, 
                           NextDigits ++ [Digit], 
                           Tail,  
                           MatchLength);
        false -> 
            {AddToPrefix,NextCC} = split_on_first_symbol(PotentialCC),
            [_FirstDigit|NextDigits] = DigitsOnly, 
            do_filter_line(Prefix ++ AddToPrefix, 
                           NextCC ++ [Digit], 
                           NextDigits ++ [Digit], 
                           Tail,  
                           MatchLength) 
    end; 
do_filter_line(Prefix, PotentialCC, DigitsOnly, [Digit|Tail], MatchLength) when Digit >= $0, Digit =< $9 ->
    do_filter_line(Prefix, PotentialCC ++ [Digit], DigitsOnly ++ [Digit], Tail,  MatchLength); 
do_filter_line(Prefix, PotentialCC, DigitsOnly, [Space|Tail], MatchLength) when Space =:= 32 ->
    do_filter_line(Prefix, PotentialCC ++ [Space], DigitsOnly, Tail, MatchLength);
do_filter_line(Prefix, PotentialCC, DigitsOnly, [Hyphen|Tail], MatchLength) when Hyphen =:= $- ->
    do_filter_line(Prefix, PotentialCC ++ [Hyphen], DigitsOnly, Tail, MatchLength);
do_filter_line(Prefix, PotentialCC, _DigitsOnly, [Char|Tail], MatchLength) ->
    do_filter_line(Prefix ++ PotentialCC ++ [Char], "", "",  Tail, MatchLength);
do_filter_line(Prefix, PotentialCC, DigitsOnly, [], _MatchLength) ->
    case is_luhny_checked(DigitsOnly) of 
        true -> Prefix ++ replace_digits(PotentialCC);
        false -> Prefix ++ PotentialCC
    end.

is_luhny_checked(DigitsOnly) when length(DigitsOnly) >= 14, length(DigitsOnly) =< 16 -> 
    Sum = lists:foldl(fun( X, S ) -> 
                          sum_up_digits( integer_to_list(X) ) + S 
                      end, 0, double_every_second_digit(DigitsOnly)),
    Sum rem 10 =:= 0;
is_luhny_checked(_) -> false.

sum_up_digits(AsciiDigitsList) -> lists:foldl(fun(X,Sum) -> Sum + (X - 48) end, 0, AsciiDigitsList).

double_every_second_digit(DigitsOnly) -> do_double_every_other(1, DigitsOnly).
do_double_every_other(2, [DigitAscii|Tail]) -> [(DigitAscii - 48) * 2] ++ do_double_every_other(1, Tail);
do_double_every_other(1, [DigitAscii|Tail]) -> [(DigitAscii - 48)] ++ do_double_every_other(2, Tail);                                           
do_double_every_other(_, []) -> [].

split_on_first_symbol(List) -> split_on_first_symbol([],List).
split_on_first_symbol(Acc, [Digit|Tail]) when Digit > 47, Digit < 58 -> { Acc ++ [Digit], Tail };
split_on_first_symbol(Acc, [X|Tail]) when X == $X -> { Acc ++ [X], Tail };
split_on_first_symbol(Acc, [Char|Tail]) ->  split_on_first_symbol(Acc ++ [Char], Tail).

replace_digits([Digit|Tail]) when Digit > 47, Digit < 58 -> "X" ++ replace_digits(Tail);
replace_digits([Char|Tail]) -> [Char] ++ replace_digits(Tail);
replace_digits([]) -> [].

do_merge([CharM|TailM], [Char|Tail]) when CharM =:= Char -> [Char] ++ do_merge(TailM,Tail); 
do_merge([_CharM|TailM], [_Char|Tail]) ->  "X" ++ do_merge(TailM,Tail); 
do_merge([], []) -> [].
