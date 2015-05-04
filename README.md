iOverview:
  This project started when I wanted to run some git command via scala on
  multiple repositories.

  Since then, a few separate pieces have started to form:
  -Executable: Give this class a program and parameters that it should execute
  on the command line
  -Client: Has an immutable program and default parameters, and allows you to
  execute commands with additional paramters
  -Repo: Client that allows you to run various advanced command sequences on a
  Git repository
  -GitDispatcher: Manages the different jobs that need to take place for the
  whole sequence to complete.
  
Currently, the project aims to give the user a handful of complex actions that will analyze their repos and give them useful graphs to view.

Interesting/Novel Technologies Used:
  -Akka Actors
  -Scala External Commands

New Concepts:
  -Designing reusable Client traits
  -Dispatching Futures to work on different repos
  -Passing message objects between actors

Lessons learned:
  -Implicits will very quickly confuse things unless you really know why you
  are using them.
  -Restricting communication between actors to defined classes/objects has
  forced me to design better APIs 

Overview:
  This project started when I wanted to run some git command via scala on
  multiple repositories.

  Since then, a few separate pieces have started to form:
  -Executable: Give this class a program and parameters that it should execute
  on the command line
  -Client: Has an immutable program and default parameters, and allows you to
  execute commands with additional paramters
  -Repo: Client that allows you to run various advanced command sequences on a
  Git repository
  -GitDispatcher: Manages the different jobs that need to take place for the
  whole sequence to complete.

Interesting/Novel Technologies Used:
  -Akka Actors
  -Scala External Commands

New Concepts:
  -Designing reusable Client traits
  -Dispatching Futures to work on different repos
  -Passing message objects between actors

Lessons learned:
  -Implicits will very quickly confuse things unless you really know why you
  are using them.
  -Restricting communication between actors to defined classes/objects has
  forced me to design better APIs 

