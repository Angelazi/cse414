# CSE 414 Homework 1: SQL Basics with SQLite, Git

**Objectives:** To create and manipulate tables in sqlite3 and write simple queries using SQL. 
To use git as a workflow tool for cloning, editing, and submitting homework.

**Assignment tools:** [SQLite 3](https://www.sqlite.org/)

**Assigned date:** January 6, 2020

**Due date:** January 13, 2020, 11:00pm. You have 1 week for this homework.

**Questions:** Make sure your post them on the [discussion board](https://piazza.com/class/k46r2oee1lb2vk).

**Collaboration:** This is a partner assignment. Please collaborate with your partner fully.
You may discuss homework topics and provide assistance to other classmates, but every partnership must write their own submissions.

**What to turn in:**

One file per each of the question below, containing your SQL and SQLite commands, 
and SQL comments for your responses that are not in SQL (i.e., *submit a `.sql` file that 
can be executed directly against the database system*). 

No need to include inputs or outputs. Name each file `hw1-q1.sql`, `hw1-q2.sql`, etc. 
You will need to learn how to write SQL comments if you have not done so before. 

Turn in your solution on [CSE's GitLab](https://gitlab.cs.washington.edu). 
See [submission instructions](#submission) below.


**Motivation:** 
We will use [SQLite](https://www.sqlite.org) for this assignment. 
SQLite is a portable software library that implements a SQL database engine. 
We will use SQLite in this assignment because it offers an extremely lightweight method to 
create and analyze structured datasets (by structured we mean datasets in the form of tables 
rather than, say, free text). Using SQLite is a minimal hassle approach to realizing the 
benefits of a relational database management system. 

Of course, SQLite does not do everything, but we will get to that point in later assignments. 
In the meantime, you can also learn [when to use SQLite and when not to use it](http://www.sqlite.org/whentouse.html).

**Resources:**

- Some important SQLite commands:
  - To view help contents: `.help`
  - To view a list of all your tables: `.tables`
  - To exit: `.exit` 
  - To run a script: `.read scriptName.sql`

- [A simple guide](http://www.pantz.org/software/sqlite/sqlite_commands_and_general_usage.html) for commonly used command-line functions in SQLite.

- [More information](http://www.sqlite.org/sqlite.html) on formatting output in SQLite. 

- [An index](http://www.sqlite.org/lang.html) of more detailed information for SQL commands in SQLite.

- A [SQL style guide](http://www.sqlstyle.guide/) in case you are interested (FYI only). We haven't covered all of these components yet. So long as your queries aren't all on one line and difficult to read, your style is OK for this assignment.


**Running SQLite:**

To run SQLite do the following:
- Mac OS X or Linux: open a terminal and type `sqlite3` (once installed)
- Windows: there are two reasonable options:
  - Install the stand-alone windows program from the [SQLite web site](https://www.sqlite.org) 
  (precompiled windows command-line shell on the [download](http://www.sqlite.org/download.html) page)
  - (maybe a bit more complicated): Install [cygwin](http://www.cygwin.com/) to get a 
  linux command shell, then open cygwin and type `sqlite3` 
  (you may have to install it by running setup →  database → sqlite3; 
  it is probably already installed in the CSE labs).


## Problems (87 points)

1. (20 points) First, create a simple table using the following steps:
  - Create a table Edges(Source, Destination) where both Source and Destination are integers.
  - Insert the tuples `(8,5)`, `(6,22)`, `(1,3)`, and `(5,5)`
  - Write a SQL statement that returns all attributes of all tuples.
  - Write a SQL statement that returns only column Source for all tuples.
  - Write a SQL statement that returns all attributes of all tuples where Source > Destination.
  - Now insert the tuple `('-1','2000')`. Do you get an error? Why? 
  This is a tricky question, you might want to [check the documentation](http://www.sqlite.org/datatype3.html).
  Please write whether you get an error or not in a SQL comment, alongside your explanation for why this is.


2. (15 points) Next, you will create a table with attributes of types integer, varchar, date, and Boolean. 
However, SQLite does not have date and Boolean: you will use `text` and `int` instead. Some notes:
  - 0 (false) and 1 (true) are the values used to interpret Booleans.
  - Date strings in SQLite are in the form: 'YYYY-MM-DD'.  
Examples of valid date strings include: `'1988-01-15'`, `'0000-12-31'`, and `'2011-03-28'`.  
Examples of invalid date strings include: `'11-11-01'`, `'1900-1-20'`, `'2011-03-5'`, and `'2011-03-50'`.
  - Examples of date operations on date strings (feel free to try them): 
  ``` 
  select date('2011-03-28');
  select date('now');
  select date('now', '-5 year');
  select date('now', '-5 year', '+24 hour');
  select case when date('now') < date('2020-03-13') then 'Taking classes' when date('now') < date('2020-03-20') then 'Exams' else 'Vacation' end;
  --What does this last query do? (no need to turn in your answer)  
  ```
Create a table called `MyRestaurants` with the following attributes (you can pick your own names for the attributes; just make sure it is clear which one is for which): 
 - Name of the restaurant: a `varchar` field
 - Type of food they make: a `varchar` field
 - Distance (in minutes) from your house: an `int`
 - Date of your last visit: a `varchar` field, interpreted as date
 - Whether it offers vegetarian menu options: an `int`, interpreted as a Boolean

3. (13 points) 
Insert at least five tuples using the SQL INSERT command (as many times as necessary to insert at least five tuples). 
Insert at least one restaurant that offers vegetarian options, at least one restaurant that does not offer vegetarian options, 
and at least one restaurant where you leave the "vegetarian" field `NULL`.

4. (13 points) 
Write a SQL query that returns all attributes of all restaurants in your table. Experiment with three variations of SQLite's 
output formats and show the command you use to format the output along with your query:
  * Turn column headers on and output the results in these 3 formats:
    - print the results in comma-separated form
    - print the results in list form, delimited by "` | `"
    - print the results in column form, and set column width to 15

5. (13 points) 
Write a SQL query that returns only the *name* and *distance* of all restaurants within and 
including 20 minutes of your house. The query should list the restaurants in alphabetical order of names.

6. (13 points) 
Write a SQL query that returns the *name* and *date of last visit* of all restaurants that offer vegetarian options but have not visited since more than 3 months ago. Use the date() function to calculate the date 3 months ago.



## Submission with git to GitLab (13 points)
<a name="submission"></a>

To earn full credit on this section, you must clone, complete, commit, and push your solutions through `git` to your assignment repository on GitLab. You must also name your files correctly: `hw1-q1.sql`, ...


We will use `git`, a source code control tool, for distributing and submitting homework assignments in this class.
This will allow you to download the code and instructions for the homework, 
and also submit homeworks in a standardized format that will streamline grading.

It is recommended to regularly use `git` to create and push commits as you work on each homework assignment.
This is a good workflow practice to become familiar with and is common in industry.

Course git repositories will be hosted as a repository in [CSE's GitLab](https://gitlab.cs.washington.edu/).
Your GitLab submission repository is private to you and the course instructors.

### Installing Git on Windows
If you're using Windows, an easy way to use git (with handy right-click menus) is

1. Install [Git for Windows](https://gitforwindows.org/). Default installation options are fine.
2. Install [TortoiseGit](https://tortoisegit.org/download/). Use your real name and email.
3. Now you can right-click in a folder and click "git clone".


### Getting started with Git

There are numerous guides on using `git` that are available. They range from interactive to text-based. 
Find one that works for your learning style and experiment -- making mistakes and fixing them is a great way to learn. 
Here is a [link to resources](https://help.github.com/articles/what-are-other-good-resources-for-learning-git-and-github) 
that GitHub suggests starting with. 

Git may already be installed in your environment; if it's not, you'll need to install it first. 
For `bash`/Linux environments, git should be a simple `apt-get` / `yum` / etc. install. 
More detailed instructions may be [found here](http://git-scm.com/book/en/Getting-Started-Installing-Git).
Git is already installed on the CSE linux machines.

If you are using Eclipse or IntelliJ, many versions come with git already configured. 
The instructions will be slightly different than the command line instructions listed but will work 
for any OS. For Eclipse, detailed instructions can be found at 
[EGit User Guide](http://wiki.eclipse.org/EGit/User_Guide) or the
[EGit Tutorial](http://eclipsesource.com/blogs/tutorials/egit-tutorial).


### Cloning your repository for homework assignments

We have created a git repository that you will use to commit and submit your the homework assignments. 
This repository is hosted on the [CSE's GitLab](https://gitlab.cs.washington.edu) , 
and you can view it by visiting the GitLab website at 
`https://gitlab.cs.washington.edu/cse414-20wi/hw1/cse414-hw1-[NETIDS]`.

NOTE: Replace `[NETIDS]` with the UW network IDs for you and your partner. 
This depends on your team. Please see your team assignment.

We will create a **new repository** for you for each of the homework assignments this quarter, 
so if you don't see this repository or are unable to access it, let us know immediately!

The first thing you'll need to do is set up a SSH key to allow communication with GitLab:

1.  If you don't already have one, generate a new SSH key. See [these instructions](http://doc.gitlab.com/ce/ssh/README.html) for details on how to do this.
2.  Visit the [GitLab SSH key management page](https://gitlab.cs.washington.edu/profile/keys). You'll need to log in using your UW account.
3.  Click "Add SSH Key" and paste in your **public** key into the text area.

While you're logged into the GitLab website, browse around to see which projects you have access to. 
You should have access to `cse414-hw1-[NETIDS]`.
Spend a few minutes getting familiar with the directory layout and file structure. For now nothing will
be there except for the `hw1` directory with these instructions.

We next want to move the code from the GitLab repository onto your local file system. 
To do this, you'll need to clone the repository by issuing the following commands on the command line:

```sh
$ cd [directory that you want to put your 414 assignments]
$ git clone git@gitlab.cs.washington.edu:cse414-20wi/hw1/cse414-hw1-[NETIDS].git
$ cd cse414-hw1-[NETIDS]
```

This will make a complete replica of the repository locally. If you get an error that looks like...

```sh
Cloning into 'cse414-hw1-[NETIDS]'...
Permission denied (publickey).
fatal: Could not read from remote repository.
```

... then there is a problem with your GitLab configuration. Check to make sure that your GitLab username matches the repository suffix, that your private key is in your SSH directory (`~/.ssh`) and has the correct permissions, and that you can view the repository through the website.

Cloning will make a complete replica of the homework repository locally. Any time you `commit` and `push` your local changes, they will appear in the GitLab repository.  Since we'll be grading the copy in the GitLab repository, it's important that you **remember to push all of your changes**!


### Submitting your assignment

You may submit your code multiple times; we will use the latest version you submit that arrives before the deadline (including late days).
(A reminder: for homework 1, each of your .sql files must be executable by sqlite.)
Your directory structure should look like this after you have completed the assignment: 

```sh
cse414-hw1-[NETIDS]
\-- README.md   # this is the file that you are currently reading
\-- hw1-q1.sql  # your solution to question 1
\-- hw1-q2.sql  # etc.
\-- hw1-q3.sql
\-- hw1-q4.sql
\-- hw1-q5.sql
\-- hw1-q6.sql
```

**Important**: In order to add your solutions to the git repo, you need to explicitly

```sh
$ git add hw1-q1.sql hw1-q2.sql hw1-q3.sql hw1-q4.sql hw1-q5.sql hw1-q6.sql
```

Then you can create a local commit for your new (or updated) files and push your local commit to GitLab.

```sh
$ git commit -a -m 'my latest changes are here (or any message you want)'
$ git push
```

The flag `-a` means "commit all changes" (the easiest way); you can also manually select which files you want to commit.  Commit and push as often as you want to save your homework on gitlab, before the deadline. 

Finally, go to the GitLab website and browse to your repository. Check to see that your files have successfully pushed to the website.

The criteria for your homework being submitted on time is that your code must be pushed by the due date and time. This means that if one of the TAs or the instructor were to open up GitLab, they would be able to see your solutions on the GitLab web page. Only the final version of your code will be graded.

**Just because your code has been committed on your local machine does not mean that it has been submitted -- it needs to be on GitLab!**

#### Final Word of Caution!

Git is a distributed version control system. This means everything operates offline until you run `git pull` or `git push`. This is a great feature.

The bad thing is that you may **forget to `git push` your changes**. This is why we strongly, strongly suggest that you **check GitLab to be sure that what you want us to see matches up with what you expect**.  As a second sanity check, you can re-clone your repository in a different directory to confirm the changes:

```sh
$ git clone git@gitlab.cs.washington.edu:cse414-20wi/hw1/cse414-hw1-[NETIDS].git confirmation_directory
$ cd confirmation_directory
$ # ... make sure everything is as you expect ...
```
As a last resort, if git is not working properly, consider using the "file upload" feature on the GitLab website to add your solution to your repository.

