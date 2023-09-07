import java.util.concurrent.Semaphore;

import TSim.*;

public class Lab1 {
  TSimInterface tsi = TSimInterface.getInstance();
  Semaphore[] sem = new Semaphore[3];
  {
    for (int i = 0; i < 3; i++) {
      sem[i] = new Semaphore(1, true);
    }
  }

  public Lab1(int speed1, int speed2) {
    Train train1 = new Train(1, speed1, 0, sem);
    Train train2 = new Train(2, speed2, 1, sem);
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

  public class Train extends Thread {
    // variabels
    private int id;
    private int speed;
    private int position;
    private Semaphore[] sem;

    // constructor
    public Train(int id, int speed, int position, Semaphore[] sem) {
      this.id = id;
      this.speed = speed;
      this.position = position;
      this.sem = sem;
    }

    public int[] nextSensor(int x, int y, int next_x, int next_y, int id, int sem_nr) {
      // block semaphore until next sensor is reached
      try {
        while (!(x == next_x && y == next_y)) {
          SensorEvent next_sen = tsi.getSensor(id);
          x = next_sen.getXpos();
          y = next_sen.getYpos();
        } // check that it reached the next sensor
        System.out.println("out");

        sem[sem_nr].release();
      } catch (CommandException | InterruptedException e) {
        e.printStackTrace();
      }
      return new int[] { x, y };
    }

    // run
    public void run() {
      try {
        tsi.setSpeed(id, speed);
        while (true) {

          SensorEvent sensor = tsi.getSensor(id);
          int x = sensor.getXpos();
          int y = sensor.getYpos();
          tsi.setSpeed(id, 0); // stop the train untill it passes sensor

          if (x == 7 && y == 3) { // upper sensor
            tsi.setSpeed(id, speed);
          }
          // cross sensors
          else if (x == 6 && y == 6) // senor to the left of the cross
          {
            if (position == 0) {

              sem[0].acquire();
              tsi.setSpeed(id, speed);
              x = nextSensor(x, y, 11, 7, id, 0)[0];
              y = nextSensor(x, y, 11, 7, id, 0)[1];
            } else {
              tsi.setSpeed(id, speed);
            }

          } else if (x == 11 && y == 7) // senor to the right of the cross
          {
            if (position == 1) {
              sem[0].acquire();
              tsi.setSpeed(id, speed);
              x = nextSensor(x, y, 6, 6, id, 0)[0];
              y = nextSensor(x, y, 6, 6, id, 0)[1];
            } else {
              tsi.setSpeed(id, speed);
            }
          } else if (x == 8 && y == 5) { // sensor upp the cross
            if (position == 0) {
              sem[0].acquire();
              tsi.setSpeed(id, speed);
              x = nextSensor(x, y, 11, 8, id, 0)[0];
              y = nextSensor(x, y, 11, 8, id, 0)[1];
            } else {
              tsi.setSpeed(id, speed);
            }
          } else if (x == 11 && y == 8) { // sensor down the cross
            if (position == 1) {

              sem[0].acquire();
              tsi.setSpeed(id, speed);
              x = nextSensor(x, y, 8, 5, id, 0)[0];
              y = nextSensor(x, y, 8, 5, id, 0)[1];
            } else {
              tsi.setSpeed(id, speed);
            }
          }
          // sensors near the upper right switch
          else if (x == 14 && y == 7) // sensor to left of switch
          {
            if (position == 0) {
              sem[1].acquire();
              System.out.println("1");
              tsi.setSpeed(id, speed);

              tsi.setSwitch(17, 7, 2); // 2 : up , 1 : down
              if (sensor.getStatus() == SensorEvent.ACTIVE) { // better condition?
                tsi.setSwitch(15, 9, 1);
                x = nextSensor(x, y, 13, 10, id, 1)[0];
                y = nextSensor(x, y, 13, 10, id, 1)[1];

              } else {
                tsi.setSwitch(15, 9, 2);
                x = nextSensor(x, y, 12, 9, id, 1)[0];
                y = nextSensor(x, y, 12, 9, id, 1)[1];
              }
            } else {
              tsi.setSpeed(id, speed);
            }
          } else if (x == 19 && y == 8) // sensor to right of switch
          {
            if (position == 1) {
              sem[1].acquire();
              tsi.setSpeed(id, speed);

              tsi.setSwitch(17, 7, 1); // 1 down 2 up
              x = nextSensor(x, y, 15, 8, id, 1)[0];
              y = nextSensor(x, y, 15, 8, id, 1)[1];
            } else {
              tsi.setSpeed(id, speed);
            }
          } else if (x == 15 && y == 8) // sensor to bottom of switch
          {
            if (position == 0) {
              sem[1].acquire();
              tsi.setSpeed(id, speed);

              tsi.setSwitch(17, 7, 1); // 1 down 2 up
              x = nextSensor(x, y, 19, 8, id, 1)[0];
              y = nextSensor(x, y, 19, 8, id, 1)[1];
            } else {
              tsi.setSpeed(id, speed);
            }
          }
          // sensors near the bottom right
          else if (x == 12 && y == 9) // sensor to left of switch
          {
            if (position == 1) {
              sem[1].acquire();
              System.out.println("2");
              tsi.setSpeed(id, speed);

              tsi.setSwitch(15, 9, 2); // 2 up 1 down
              x = nextSensor(x, y, 15, 8, id, 1)[0];
              y = nextSensor(x, y, 15, 8, id, 1)[1];

            } else {
              tsi.setSpeed(id, speed);
            }

          } else if (x == 13 && y == 10) // sensor to left bottom of switch
          {
            if (position == 1) {
              sem[1].acquire();
              tsi.setSpeed(id, speed);

              tsi.setSwitch(15, 9, 1); // 2 up 1 down
              x = nextSensor(x, y, 18, 9, id, 1)[0];
              y = nextSensor(x, y, 18, 9, id, 1)[1];
            } else {
              tsi.setSpeed(id, speed);
            }

          } else if (x == 18 && y == 9) // sensor to right of switch, connected to 14,7
          {
            if (position == 0) {
              sem[1].acquire();
              tsi.setSpeed(id, speed);

              tsi.setSwitch(15, 9, 2); // 2 up 1 down
            }
            tsi.setSpeed(id, speed);
            sem[1].release();
          }
          // sensors near the upper left switch
          else if (x == 1 && y == 9) // sensor to left of switch
          {
            if (position == 1) {
              sem[2].acquire();
              tsi.setSwitch(4, 9, 1); // 2 down 1 up
            }
            tsi.setSpeed(id, speed);
          } else if (x == 6 && y == 10) // bottom sensor to switch
          {
            if (position == 0) {
              sem[2].acquire();
              tsi.setSwitch(4, 9, 2); // 2 down 1 up
            }
            tsi.setSpeed(id, speed);
          } else if (x == 7 && y == 9) // upper sensor to switch
          {
            if (position == 0) {
              sem[2].acquire();
              tsi.setSwitch(4, 9, 1); // 2 down 1 up
            }
            tsi.setSpeed(id, speed);
          }
          // sensors near the bottom left switch
          else if (x == 1 && y == 10) // sensor to left of switch
          {
            if (position == 0) {
              sem[2].acquire();
              tsi.setSwitch(3, 11, 2); // 2 down 1 up
            }
            tsi.setSpeed(id, speed);
          } else if (x == 6 && y == 11) // upper right sensor to switch
          {
            if (position == 1) {
              System.out.println("in");
              sem[2].acquire();
              System.out.println("ssa");
              tsi.setSwitch(3, 11, 1); // 2 down 1 up
            }
            tsi.setSpeed(id, speed);
          } else if (x == 4 && y == 13) // bottom right sensor to switch
          {
            if (position == 1) {
              sem[2].acquire();
              tsi.setSwitch(3, 11, 2); // 2 down 1 up
            }
            tsi.setSpeed(id, speed);
          }
          // Sensors near the houses
          else if (x == 14 && y == 13) // sensor near bottom house
          {
            if (speed > 0) {
              tsi.setSpeed(id, 0);
              position = 1;
              speed = -1 * speed;
              sleep(1000 + (20 * Math.abs(speed)));

            }
            tsi.setSpeed(id, speed);

          } else if (x == 14 && y == 5) // sensor near bottom house
          {
            if (speed < 0) { // does not work
              tsi.setSpeed(id, 0);
              position = 0;
              speed = -1 * speed;
              sleep(1000 + (20 * Math.abs(speed)));
            }
            tsi.setSpeed(id, speed);
          }
          sem[2].release();
        }
      } catch (CommandException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
