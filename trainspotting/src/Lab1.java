import java.util.concurrent.Semaphore;

import TSim.*;

public class Lab1 {
  TSimInterface tsi = TSimInterface.getInstance();
  Semaphore[] sem = new Semaphore[9];
  {
    for (int i = 0; i < 9; i++) {
      sem[i] = new Semaphore(1, true); // creating semaphore
      // semaphore 0 = first upmost track
      // semaphore 1 = upper trainstation track
      // semaphore 2 = crossing
      // semaphore 3 = critical right section
      // semaphore 4 = upper middle parallel track
      // semaphore 5 = bottom middle parallel track
      // semaphore 6 = critical left section
      // semaphore 7 = over trainstation track
      // semaphore 8 = bottom trainstation track
    }
  }

  public Lab1(int speed1, int speed2) {

    try {
      Train train1 = new Train(1, speed1, false, sem);// train 1 is going down
      Train train2 = new Train(2, speed2, true, sem);// train 2 is going up
      train1.start(); // starting the thread
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
    private boolean going_up; // direction of the train
    private Semaphore[] sem;

    public Train(int id, int speed, boolean going_up, Semaphore[] sem)// constructor
    {
      this.id = id;
      this.speed = speed;
      this.going_up = going_up;
      this.sem = sem;
    }

    public void nextSensor(int x, int y, int next_x, int next_y, int sem_nr) // block semaphore
    {
      try {
        while (!(x == next_x && y == next_y)) { // as long as the train didn't reach the next sensor
          SensorEvent next_sen = tsi.getSensor(id); // look if the while loop is needed
          x = next_sen.getXpos();
          y = next_sen.getYpos();
        } // check that it reached the next sensor
        sem[sem_nr].release();
      } catch (CommandException | InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void sen_before_sw(int sem_nr, int sw_x, int sw_y, int sw_dir) // acquire the ticket and change the switch
    {
      try {
        sem[sem_nr].acquire();
        tsi.setSwitch(sw_x, sw_y, sw_dir);
        tsi.setSpeed(id, speed);
      } catch (InterruptedException | CommandException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    public void sen_cross(boolean state, int x, int y, int next_x, int next_y) // cross semaphore logic
    {
      try {
        if (going_up == state) {
          sem[2].acquire();
          tsi.setSpeed(id, speed);
          nextSensor(x, y, next_x, next_y, 2);
        } else
          tsi.setSpeed(id, speed);
      } catch (InterruptedException | CommandException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    public void end_track_sensor(boolean old_state) // stop the train and switch its direction then start it again
    {
      try {
        if (going_up == old_state) {
          sleep(1000 + (20 * Math.abs(speed)));
          going_up = !going_up;
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
        if (!going_up)
          sem[0].acquire(); // starting state
        else
          sem[7].acquire();
        while (true) // the main program
        {
          SensorEvent sensor = tsi.getSensor(id);
          int x = sensor.getXpos();
          int y = sensor.getYpos();
          tsi.setSpeed(id, 0); // stop the train if sensor doesnt allow it

          // snesors before each end
          if ((x == 15 && y == 3) || (x == 14 && y == 5)) // upper end sensors, direction: going up to going down
            end_track_sensor(true);
          else if ((x == 15 && y == 11) || (x == 14 && y == 13)) // lower end sensors, direction: going down to going up
            end_track_sensor(false);

          // cross sensors
          else if (x == 6 && y == 6) // senor to the left of the cross
            sen_cross(false, x, y, 11, 7);
          else if (x == 11 && y == 7) // senor to the right of the cross
            sen_cross(true, x, y, 6, 6);
          else if (x == 8 && y == 5) // sensor upp the cross
            sen_cross(false, x, y, 11, 8);
          else if (x == 11 && y == 8) // sensor down the cross
            sen_cross(true, x, y, 8, 5);

          //
          else if (x == 14 && y == 7) // sensor to left of switch1 : down
          {
            if (!going_up) {
              sen_before_sw(3, 17, 7, 2);
              nextSensor(x, y, 19, 9, 0);
            } else
              tsi.setSpeed(id, speed);
          }
          // upper station track
          else if (x == 15 && y == 8) // sensor to bottom of switch
          {
            if (!going_up) {
              sen_before_sw(3, 17, 7, 1);
              nextSensor(x, y, 19, 9, 1);
            } else
              tsi.setSpeed(id, speed);
          }
          // right critical section sensor
          else if (x == 19 && y == 9) { // sensor to right of switch
            tsi.setSpeed(id, speed);
            if (!going_up) {
              if (sem[4].tryAcquire()) { // upper middle parallel track
                tsi.setSwitch(15, 9, 2); // 2 : up , 1 : down
                nextSensor(x, y, 12, 9, 3);

              } else { // lower middle parallel track
                sem[5].acquire();
                tsi.setSwitch(15, 9, 1);
                nextSensor(x, y, 13, 10, 3);
              }
            } else {
              if (sem[1].tryAcquire()) { // upper middle parallel track
                tsi.setSwitch(17, 7, 1); // 2 : up , 1 : down
                nextSensor(x, y, 15, 8, 3);
              } else { // lower middle parallel track
                sem[0].acquire();
                tsi.setSwitch(17, 7, 2); // 2 : up , 1 : down
                nextSensor(x, y, 14, 7, 3);
              }
            }
          }
          // upper parallel middle section
          else if (x == 12 && y == 9) // sensor to right
          {
            if (going_up) {
              sen_before_sw(3, 15, 9, 2);
              nextSensor(x, y, 19, 9, 4);
            } else
              tsi.setSpeed(id, speed);
          } // left sensor to upper middle track
          else if (x == 7 && y == 9) {
            if (!going_up) {
              sen_before_sw(6, 4, 9, 1);
              nextSensor(x, y, 1, 10, 4);
            } else
              tsi.setSpeed(id, speed);
          }
          // bottom parallel middle section
          else if (x == 13 && y == 10) { // right sensor
            if (going_up) {
              sen_before_sw(3, 15, 9, 1);
              nextSensor(x, y, 19, 9, 5);
            } else
              tsi.setSpeed(id, speed);

          } else if (x == 7 && y == 10) {
            if (!going_up) {
              sen_before_sw(6, 4, 9, 2);
              nextSensor(x, y, 1, 10, 5);
            } else
              tsi.setSpeed(id, speed);
          } else if (x == 1 && y == 10) {
            tsi.setSpeed(id, speed);
            if (going_up) {
              if (sem[4].tryAcquire()) { // upper middle parallel track
                tsi.setSwitch(4, 9, 1);
                nextSensor(x, y, 7, 9, 6);
              } else { // lower middle parallel track
                sem[5].acquire();
                tsi.setSwitch(4, 9, 2);
                nextSensor(x, y, 7, 10, 6);
              }
            } else { // going down
              if (sem[8].tryAcquire()) { // upper middle parallel track
                tsi.setSwitch(3, 11, 2);
                nextSensor(x, y, 4, 13, 6);
              } else { // lower middle parallel track
                sem[7].acquire();
                tsi.setSwitch(3, 11, 1);
                nextSensor(x, y, 6, 11, 6);
              }
            }
          }
          // bottom train tracks
          else if (x == 6 && y == 11) {
            if (going_up) {
              sen_before_sw(6, 3, 11, 1);
              nextSensor(x, y, 1, 10, 7);
            } else
              tsi.setSpeed(id, speed);
          }

          // bottom station track
          else if (x == 4 && y == 13) {
            if (going_up) {
              sen_before_sw(6, 3, 11, 2);
              nextSensor(x, y, 1, 10, 8);
            } else
              tsi.setSpeed(id, speed);
          }
        }
      } catch (CommandException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}