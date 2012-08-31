public class Demo {

   /**
    * @param args
    * @throws InterruptedException 
    */
   public static void main(String[] args) throws InterruptedException {
      int count = 0;
      while (count++ < 50) {
         System.out.println(count);
         Thread.sleep(100);
      }
   }

}
