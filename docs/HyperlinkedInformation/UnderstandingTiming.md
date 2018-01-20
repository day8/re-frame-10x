This document explains key aspects of the "Timing" tab. 

## First, The Warnings

There are two issues with the numbers you are looking at in the "Timing" tab:  

1.  Accurately timing something in the browser is almost
    a fool's errand. One moment it takes 1ms and the next it 
    takes 10ms, and you’ll never know why. Noisy.   

    So, do not ever believe one set of timings numbers.
    
    In the future, we'd like to add a 'Run It Again' button, which 
    you can click a few times to see if you get stable numbers. 
    Perhaps you'll beat us to it, and create a PR for this 
    feature? 
    
2.  Don't freak out about any apparent slowness, yet.

    You're currently running a dev build, right, not the 
    production build.
    And I'm guessing you're also running a dev build 
    of React.
    
    Also, using `re-frame-trace` will slow things 
    down too, what with all that creating and analysing of trace.
    
    So, run the production version of your app first before 
    deciding you have a performance problem. Something that 
    takes 100ms in dev might take 20ms in prod.
    
    This Timing panel is not really about absolute numbers so 
    much as the relative time taken to do the "parts" of an Epoch. 
    And, even then, be careful of these relative numbers because
    of point 1 (above).
    
## Know Your Epoch Timeline

You'll understand the contents of the Timings tab better if you 
understand how an event is processed over time. The following is from 
the re-frame project itself. 

<img src="https://raw.githubusercontent.com/Day8/re-frame/master/images/epoch.png">

