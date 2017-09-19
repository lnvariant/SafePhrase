package com.invariant.safephrase.source.other;

import java.util.ArrayList;
import java.util.List;

public class BinarySearch implements Searcher {

    /**
     * Return true if the given word is inside the list.
     *
     * @param list the list to find the value in
     * @param value the value to find
     * @return true if the word is in the list
     */
    @Override
    public boolean search(List<String> list, String value) {

        /* If the list is empty then we didn't find the word */
        if(list.size() == 0){
            return false;
        }

        int half_point = list.size() / 2;
        String half_point_word = list.get(half_point);

        /* If the word matches the half point word, then we found it */
        if(half_point_word.matches(".*\\b" + value.toUpperCase() + "\\b.*")){
            return true;
        }
        /* Otherwise, if it is less than the half point word, then search from begin to half point */
        else if(value.toUpperCase().compareTo(half_point_word) < 0){
            ArrayList<String> new_list = new ArrayList<String>();
            new_list.addAll(list.subList(0, half_point));
            return search(new_list, value);
        }
        /* Otherwise, if it iss more than half point word, then search from half to end */
        else if(value.toUpperCase().compareTo(half_point_word) > 0){
            ArrayList<String> new_list = new ArrayList<String>();
            new_list.addAll(list.subList(half_point + 1, list.size()));
            return search(new_list, value);
        }

            return false;
    }
}
