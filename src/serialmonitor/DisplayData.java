/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

/**
 *
 * @author fuerst
 */
public class DisplayData {
    int number;
    int direction;
    long timestamp; 
    String bytes;
    
    public DisplayData(int number, int direction, long timestamp,  String str){
      this.number = number;
      this.direction = direction;
      this.bytes = str;
      this.timestamp = timestamp;
      
    }
}
