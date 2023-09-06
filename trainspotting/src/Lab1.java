import java.util.concurrent.Semaphore;

import TSim.*;


public class Lab1
{
  TSimInterface tsi = TSimInterface.getInstance();
  Semaphore[] sem = new Semaphore[3];
  {
    for (int i = 0; i < 3; i++)
      {
        sem[i] = new Semaphore(1, true);
      }
  }
  
  public Lab1(int speed1, int speed2) {
    Train train1 = new Train(1,speed1,0,sem);
    Train train2 = new Train(2,speed2,1,sem);
    train1.start();
    train2.start();

    try {
      train1.join();
      train2.join();

    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  public class Train extends Thread
  {
    // variabels
    private int id;
    private int speed;
    private int position;
    private Semaphore[] sem;

    //constructor
    public Train(int id,int speed, int position,Semaphore[] sem )
    {
      this.id = id;
      this.speed = speed;
      this.position = position;
      this.sem = sem;
    }

    // run
    public void run()
    {
      try
      {
        tsi.setSpeed(id, speed);
        while(true)
        {
        
          SensorEvent sensor = tsi.getSensor(id);
          int x = sensor.getXpos();
          int y = sensor.getYpos();
          tsi.setSpeed(id, 0); // stop the train untill it passes sensor
          if (x == 7 && y == 3){ // upper sensor
            tsi.setSpeed(id, speed);
          }
          else if (x==6 && y == 6) // senor to the left of the cross
          {
            if (position == 0)
            {
              System.out.println("sne");
              sem[0].acquire();
              tsi.setSpeed(id, speed);
              sem[0].release();
            }
            else
            {
              System.out.println("aa");
              sem[0].release();
              tsi.setSpeed(id, speed);
            }
          }
          else if (x==11 && y == 7) // senor to the left of the cross
          {
            if (position == 1)
            {
              sem[0].acquire();
              tsi.setSpeed(id, speed);
            }
            else
            {
              sem[0].release();
              tsi.setSpeed(id, speed);
            }
          }          else if (x==14 &&y ==7) // sensor to left of the upper most right switch
          {
            sem[1].acquire();
            tsi.setSwitch(17, 7, 2); // 2 : up , 1 : down
            sem[1].release();
          }
          else if (x==19 &&y ==8) // sensor to right of upper most right switch
          {
            sem[1].acquire();
            tsi.setSwitch(17, 7, 1); //1 down 2 up
            sem[1].release();
          }
          else if (x == 12 && y==9) // sensor to left of the bottom right switch
          {
            sem[1].acquire();
            tsi.setSwitch(15, 9, 2); // 2 up 1 down
            sem[1].release();
          }
          else if (x==1 &&y==10) // sensor to left of the bottom left switch
          {
            sem[2].acquire();
            tsi.setSwitch(3, 11, 2); // 2 down 1 up
            sem[2].release();
          }
          else if (x == 6 && y == 10) // bottom sensor to the upper left switch
          {
            sem[2].acquire();
            tsi.setSwitch(4, 9, 2); // 2 down 1 up
            sem[2].release();
            
          }
          else if (x == 14 && y == 13) // sensor near bottom house
          {
            tsi.setSpeed(id, 0);
            sleep(10000);
            tsi.setSpeed(id, -speed);
          }
        }
      }
      catch(CommandException | InterruptedException  e)
      {
        e.printStackTrace();
      }
    }
  }


  
  


}

