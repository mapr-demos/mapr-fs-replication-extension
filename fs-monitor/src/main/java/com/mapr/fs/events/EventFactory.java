package com.mapr.fs.events;

public class EventFactory {

    public Event getEvent(String data) {

        if(data == null){
            return null;
        }

        if(data.equalsIgnoreCase("create")){
            return new Create();

        } else if(data.equalsIgnoreCase("delete")){
            return new Delete();

        } else if(data.equalsIgnoreCase("modify")){
            return new Modify();
        }

        return null;
    }
}
