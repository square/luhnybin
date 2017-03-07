#!/usr/bin/env escript

main(_Args) ->
    add_ebin_directory_to_path(),
    luhny_filter:process_line(io:get_line("")).

add_ebin_directory_to_path() ->
    File = escript:script_name(),
    Dir = filename:dirname(File),
    true = code:add_path(filename:join(Dir,"ebin")).