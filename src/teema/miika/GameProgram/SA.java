package teema.miika.GameProgram;

// Source: From teacher
public class SA {
  int markov;
  int called;
  final double START_PROB = 1.00;
  final double END_PROB = 0.00;
  double runningProb = START_PROB;
  double decrInProb = 0;
  
  SA ( int rounds, int updatingInterval ) {
    markov = rounds / updatingInterval;
    decrInProb = (double)markov / rounds;
    called = 0;
    System.out.println ("Markov len: " + markov );
    System.out.println ("Decrement in probablity per markov len: " + decrInProb );
  }
  public boolean accept () {
    if ( Math.random() < runningProb ) return true;
    else return false;
  }
  
  public double acceptenceProb () {
    return runningProb;
  }
  
  public void calcNewProb () {
    called++;
    if ( called % markov == 0 ) {
      runningProb -= decrInProb;
    }
    if ( runningProb < 0 ) runningProb = 0;
  }
}
