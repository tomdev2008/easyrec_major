--
-- $Author: szavrel $
-- $Revision: 14928 $
-- $Date: 2009-11-11 17:28:17 +0100 (Mi, 11 Nov 2009) $
--

//this file contains 5 identical sql statements and a lot of comments
select *
from dual;

select 
*
from 
dual; 

select --with comment
* --with comment
from //with comment
dual;#with comment

select --with comment
* --with comment
from //with comment
dual//comment again
;#with comment

select --with comment
* --with comment
from //with comment
dual;#with comment
