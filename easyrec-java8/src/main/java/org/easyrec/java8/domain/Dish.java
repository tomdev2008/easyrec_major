package org.easyrec.java8.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by admin on 2016/11/15.
 */
public class Dish {

    public static void main(String[] args) {
        List<Integer> someNumbers = Arrays.asList(1, 2, 3, 4, 5);
        Optional<Integer> firstSquareDivisibleByThree =
                someNumbers.stream()
                        .map(x -> x * x)
                        .filter(x -> x % 3 == 0)
                        .findFirst(); // 9
        //规约
        int num = someNumbers.stream().reduce(0,(a,b) -> a+b);
        int num2 = someNumbers.stream().reduce(0,Integer::sum);
        System.out.println(num2);
    }

}
