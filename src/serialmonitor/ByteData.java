/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

/**
 *
 * @author drpuzzle
 */
public class ByteData {
  
  public ByteData(){
    
  }
  
  public ByteData(long timestamp, byte singleByte){
    this.timestamp = timestamp;
    this.singleByte = singleByte;
    
  }
  public long timestamp;
  public byte singleByte;
  
}
