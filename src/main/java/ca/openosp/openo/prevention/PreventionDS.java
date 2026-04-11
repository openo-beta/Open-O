//CHECKSTYLE:OFF



package ca.openosp.openo.prevention;

//import java.io.ByteArrayInputStream;

import org.springframework.stereotype.Component;

/**
 * @author Jay Gallagher
 */
@Component
public interface PreventionDS {


    public void reloadRuleBase();


    public Prevention getMessages(Prevention p) throws Exception;


    ///
    //         URL url = Prevs.class.getResource( "prevention.drl" );
    //      log.debug(url.getFile());
    //
    //      //RuleBase ruleBase = RuleBaseBuilder.buildFromUrl( url );
    //      RuleBase ruleBase = RuleBaseLoader.loadFromUrl( url );
    //
    //
    //
    //      Prevention[] p = new Prevention[4];
    //
    //      p[0] = new Prevention("BOB","M",new GregorianCalendar(1998, Calendar.DECEMBER, 12).getTime()); //name , num imm, sex , age in months
    //      p[1] = new Prevention("RITA","F",new GregorianCalendar(2005, Calendar.JANUARY, 1).getTime());
    //      p[2] = new Prevention("GEORGE","M",new GregorianCalendar(2004, Calendar.APRIL, 25).getTime());
    //      p[3] = new Prevention("Tyler","M",new GregorianCalendar(2005, Calendar.MARCH, 23).getTime());
    //
    //
    //      PreventionItem pi = new PreventionItem("DTaP IPV",new GregorianCalendar(2005, Calendar.MARCH, 1).getTime());
    //      p[1].addPreventionItem(pi, "DTaP IPV");
    //      log.debug("should be 1 "+p[1].getNumberOfPreventionType("DTaP IPV"));
    //
    //      log.debug("should be ?? "+p[1].getHowManyMonthsSinceLast("DTaP IPV"));
    //
    //      for ( int k = 0; k < p.length; k++){
    //         long start = System.currentTimeMillis();
    //         WorkingMemory workingMemory = ruleBase.newWorkingMemory();
    //
    //         workingMemory.assertObject(p[k]);
    //
    //         workingMemory.fireAllRules();
    //         long end = System.currentTimeMillis();
    //
    //         ArrayList alist = p[k].getWarnings();
    //         log.debug(p[k].getName()+" "+"size:"+alist.size()+ " at months : "+p[k].getAgeInMonths() + " time :"+(end-start));
    //
    //         for (int i = 0; i < alist.size(); i++){
    //            String s = (String) alist.get(i);
    //            log.debug(s);
    //         }
    //
    //      }


    ////

}
 