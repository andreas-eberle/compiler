/**
 * This test is for if/while
 */

class Loops {

    public static void main ( String[] args)
    {
        int a; int b; int c; int d;

        a = -125;

        while (a < 10)
        {
            ;
            if ( a % 2 == 0 )
            {
                b = b+1;
            }
            else
            {
                c = c-1;
            }

            a = a + 3;
        }

        while ( (a = read(c)) != 0 ) /*semantic error - read is undefined*/
        {
            d = a - abs(a); /*semantic error - abs is undefined*/
        }

        if ((d > 0) || (c < 0) || x < y) /*semantic error - x,y are undefined*/
        {
            return 1;
        }

        if (true) {} else if (false) {} else {} if (true_or_false()) {{}}; /*semantic error - true_or_false is undefined*/

        /* if - chain */
        if (true) if (true) if (false) if (true) if ((null)) if (true) ;

        /* while - chain */
        while(1) while(1) while(1) while(1) { while(1) while(1) while(1) while(1); }

        while(1){{}}
        
        /* random blocks and empty statements*/
        /* the author is boooooooooored */

        {
            {
                ;{
                    ;
                };
            }
        }

        ;(     oO    ); /*semantic error - oO is undefined*/

        ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

        return 0;
    }
}