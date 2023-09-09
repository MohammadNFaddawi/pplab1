import java.util.concurrent.Semaphore;

import TSim.*;

public class Lab1 {
  TSimInterface tsi = TSimInterface.getInstance();
  Semaphore[] sem = new Semaphore[9];
  {
    for (int i = 0; i < 9; i++) {
      sem[i] = new Semaphore(1, true);
    }
  }

  public Lab1(int speed1, int speed2) {

    try {
      Train train1 = new Train(1, speed1, 0, sem);// position 0 means that the train going down
      Train train2 = new Train(2, speed2, 1, sem);// position 1 means that the train going up
      train1.start();
      train2.start();
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

    public void nextSensor(int x, int y, int next_x, int next_y, int sem_nr) {
      // block semaphore until next sensor is reached
      try {
        while (!(x == next_x && y == next_y)) {
          SensorEvent next_sen = tsi.getSensor(id);
          x = next_sen.getXpos();
          y = next_sen.getYpos();
        } // check that it reached the next sensor
        sem[sem_nr].release();
        System.out.println("semaphore " + String.valueOf(sem_nr) + " realesed");
      } catch (CommandException | InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void sen_before_sw(int sem_nr, int sw_x, int sw_y, int sw_dir) {
      try {
        sem[sem_nr].acquire();
        System.out.println("semaphore acquired " + String.valueOf(sem_nr));
        tsi.setSwitch(sw_x, sw_y, sw_dir);
        tsi.setSpeed(id, speed);
      } catch (InterruptedException | CommandException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    public void sen_cross(int pos, int x, int y, int next_x, int next_y) {
      try {
        if (position == pos) {
          sem[2].acquire();
          System.out.println("cross ticket acquired " + String.valueOf(2));
          tsi.setSpeed(id, speed);
          nextSensor(x, y, next_x, next_y, 2);

        } else {
          tsi.setSpeed(id, speed);

        }

      } catch (InterruptedException | CommandException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    public void end_track_sensor(int old_pos) // stop the train and switch its direction then start it again
    {
      try {
        if (position == old_pos) {
          sleep(1000 + (20 * Math.abs(speed)));
          position = 1 - position;
          speed = -speed;
        }
        tsi.setSpeed(id, speed);

      } catch (InterruptedException | CommandException e) {
        e.getStackTrace();
      }
    }

    // run
    public void run() {
      try {
        tsi.setSpeed(id, speed);
        if (position == 0) {
          sem[0].acquire(); // starting state
        } else if (position == 1) {
          sem[7].acquire();
        }
        while (true) {

          SensorEvent sensor = tsi.getSensor(id);
          int x = sensor.getXpos();
          int y = sensor.getYpos();
          tsi.setSpeed(id, 0); // stop the train if sensor doesnt allow it

          if ((x == 15 && y == 3) || (x == 14 && y == 5)) // direction from going up to going down
          {
            end_track_sensor(1);
          } else if ((x == 15 && y == 11) || (x == 14 && y == 13)) // direction from going down to going up
          {
            end_track_sensor(0);
          }
          // cross sensors
          else if (x == 6 && y == 6) // senor to the left of the cross
          {
            sen_cross(0, x, y, 11, 7);
          } else if (x == 11 && y == 7) // senor to the right of the cross
          {
            sen_cross(1, x, y, 6, 6);
          } else if (x == 8 && y == 5) // sensor upp the cross
          {
            sen_cross(0, x, y, 11, 8);
          } else if (x == 11 && y == 8) // sensor down the cross
          {
            sen_cross(1, x, y, 8, 5);
          }

          else if (x == 14 && y == 7) // sensor to left of switch1 : down
          {
            if (position == 0) {
              sen_before_sw(3, 17, 7, 2);
              nextSensor(x, y, 19, 9, 0);
            } else {
              tsi.setSpeed(id, speed);
            }
          } // upper station track
          else if (x == 15 && y == 8) // sensor to bottom of switch
          {
            if (position == 0) {
              sen_before_sw(3, 17, 7, 1);
              nextSensor(x, y, 19, 9, 1);
            } else {
              tsi.setSpeed(id, speed);
            }
          }
          // right critical section sensor
          else if (x == 19 && y == 9) { // sensor to right of switch
            tsi.setSpeed(id, speed);
            if (position == 0) {
              if (sem[4].tryAcquire()) { // upper middle parallel track
                System.out.println("upper middle track acquired " + String.valueOf(4));
                tsi.setSwitch(15, 9, 2); // 2 : up , 1 : down
                nextSensor(x, y, 12, 9, 3);

              } else { // lower middle parallel track
                sem[5].acquire();
                System.out.println("bottom middle track acquired " + String.valueOf(5));

                tsi.setSwitch(15, 9, 1);
                nextSensor(x, y, 13, 10, 3);
              }
            } else {
              if (sem[1].tryAcquire()) { // upper middle parallel track
                System.out.println("upper station track acquired " + String.valueOf(1));
                tsi.setSwitch(17, 7, 1); // 2 : up , 1 : down
                nextSensor(x, y, 15, 8, 3);
              } else { // lower middle parallel track
                sem[0].acquire();
                System.out.println("upper train track acquired " + String.valueOf(0));
                tsi.setSwitch(17, 7, 2); // 2 : up , 1 : down
                nextSensor(x, y, 14, 7, 3);
              }
            }
          }
          // upper parallel middle section
          else if (x == 12 && y == 9) // sensor to right
          {
            if (position == 1) {
              sen_before_sw(3, 15, 9, 2);
              nextSensor(x, y, 19, 9, 4);
            } else {
              tsi.setSpeed(id, speed);
            }
          } // left sensor to upper middle track
          else if (x == 7 && y == 9) {
            if (position == 0) {
              sen_before_sw(6, 4, 9, 1);
              nextSensor(x, y, 1, 10, 4);
            } else {
              tsi.setSpeed(id, speed);
            }
          }
          // bottom parallel middle section
          else if (x == 13 && y == 10) { // right sensor
            if (position == 1) {
              sen_before_sw(3, 15, 9, 1);
              nextSensor(x, y, 19, 9, 5);
            } else {
              tsi.setSpeed(id, speed);
            }
          } else if (x == 7 && y == 10) {
            if (position == 0) {
              sen_before_sw(6, 4, 9, 2);
              nextSensor(x, y, 1, 10, 5);
            } else {
              tsi.setSpeed(id, speed);
            }
          } else if (x == 1 && y == 10) {
            tsi.setSpeed(id, speed);
            if (position == 1) {
              if (sem[4].tryAcquire()) { // upper middle parallel track
                System.out.println("upper middle track acquired " + String.valueOf(4));
                tsi.setSwitch(4, 9, 1);
                nextSensor(x, y, 7, 9, 6);

              } else { // lower middle parallel track
                sem[5].acquire();
                System.out.println("bottom middle track acquired " + String.valueOf(5));

                tsi.setSwitch(4, 9, 2);
                nextSensor(x, y, 7, 10, 6);
              }
            } else { // going down
              if (sem[8].tryAcquire()) { // upper middle parallel track
                System.out.println("bottom station track acquired " + String.valueOf(8));
                tsi.setSwitch(3, 11, 2);
                nextSensor(x, y, 4, 13, 6);
              } else { // lower middle parallel track
                sem[7].acquire();
                System.out.println("bottom train track acquired " + String.valueOf(7));
                tsi.setSwitch(3, 11, 1);
                nextSensor(x, y, 6, 11, 6);
              }
            }
          }
          // bottom train tracks
          else if (x == 6 && y == 11) {
            if (position == 1) {
              sen_before_sw(6, 3, 11, 1);
              nextSensor(x, y, 1, 10, 7);
            } else {
              tsi.setSpeed(id, speed);
            }
          }

          // bottom station track
          else if (x == 4 && y == 13) {
            if (position == 1) {
              sen_before_sw(6, 3, 11, 2);
              nextSensor(x, y, 1, 10, 8);
            } else {
              tsi.setSpeed(id, speed);
            }
          }

        }
      } catch (CommandException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}