            **shouldReplanPath**  
              
1. we are finding footman X and Y positions
2. Also we are finding enemyfootman X and Y locations
3. And in if checks enemy is closing to footman or not. here we used Euclidean distance to find the distance between two.
4. if current path itself is null or empty we are returning false


** AstarSearch**

-> This method will be used to find the optimal path from start to goal position.
-> Will be using A* Algorithm to implement the method.
->Will return a stack of locations which will be used to find the path.