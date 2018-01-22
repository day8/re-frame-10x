This document highlights important aspects of the "Timing" tab. 

## Be Cautious And Sceptical

There are two issues with the displayed numbers: 

1.  Accurately timing something in the browser is almost
    a fool's errand. One moment it takes 1ms and the next it 
    takes 10ms, and you’ll never know why. Noisy.

    So, don't ever base your decisions on one set of timings. Run 
    the same event at least a few of times.
    
    In the future, we'd like to add a 'Run It Again' button, which 
    you can click a few times to see if you get stable numbers. 
    Perhaps you'll beat us to it, and create a PR for this 
    feature? 
    
2.  Don't freak out about any apparent slowness, yet.

    After all, you're running a dev build, right, not the 
    production build?  And I'm guessing you're also 
    running a dev build of React?
    
    And using `re-frame-trace` will slow things 
    down too, what with all that creating and analysing of trace.
    
    So, run the production version of your app first, before 
    deciding you have a performance problem. Something what 
    takes 100ms in dev might take 20ms in prod.
    
    The Timing Tab is not really about absolute numbers so 
    much as the relative time taken to do the different 
    "parts" of an Epoch. Is one View very slow for some 
    reason, compared to others?
    And, even then, remember point 1 (above). 
    
## Know Your Epoch Timeline

The Timing Tab is easier to understand once you have internalised the 
following graphic which shows how, operationally, the six dominoes play out, 
over time, within the browser.  

<img src="https://raw.githubusercontent.com/Day8/re-frame/master/images/epoch.png">

## Other Tips 

You should probably have [React DevTools](https://github.com/facebook/react-devtools)
installed because it is useful. But, it can also add drag and noise to timing results, 
so disable it when trying to get more accurate timing figures.

Here is (React 16) advice on [debugging React performance with Chrome Devtools](https://building.calibreapp.com/debugging-react-performance-with-react-16-and-chrome-devtools-c90698a522ad) 

The [re-frame.core/debug](https://github.com/Day8/re-frame/blob/master/src/re_frame/std_interceptors.cljc) middleware is relatively slow, and runs interleaved with your application's events being processed. re-frame-trace gives you the same information in the app-db panel, but saves the calculations until after your application has finished running, so you don't get the performance cost included in your timing.
