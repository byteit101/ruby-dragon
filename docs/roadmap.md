# The Future of Ruby Dragon
See below for details about upcoming releases of Ruby Dragon. If you have feedback
or want to make a suggestion, please submit an issue on the project's
[Github page](https://github.com/goatshriek/ruby-dragon).


## 1.4.0
 * [ADD] **Groovy Language Bindings**
   While initially aimed at Ruby, this project is ultimately aimed at making a
   variety of JVM-based languages available to Ghidra users. Groovy is an easy
   target, being the most Java-friendly interpreted language.


## Unallocated to a release
 * [ADD] **Complete Example Set**
   A full suite of example scripts is needed for users to quickly understand how
   to use the functionality here, and what is possible. The set of examples will
   be considered adequate when there is at least a 1-1 parity with the existing
   Python examples.
 * [ADD] **Colorization Support in Interactive Terminal**
 * [ADD] **Tab-completion Support**
 * [ADD] **Better terminal than Ghidra default**
   It would be nice to support features like colorization, and even nicer to
   simply use one that someone else has already built instead of implementing
   this ourselves.


## What you'll find here and what you wont
Ruby Dragon is under active development, and has a long list of new features and
improvements waiting to be implemented. Some of these are detailed in the issues
list on the project Github website, but this is certainly not a comprehensive
list of planned updates. Similarly, work that is in progress on new features is
tracked as a project on the Github repository, but future planned work does not
exist there either. Instead, the plans for future direction are kept here, in
the project roadmap.

Items are added to the roadmap once they have been identified, assessed for
level of effort, and prioritized based on community needs. Each item is assigned
to a semantic version, along with its change type, a description, and the
reasoning behind it. Where they exist, you will see references to issues on the
Github repository where you can go for more details on the origin of the
request. Once a version is in work, you will be able to find a corresponding
project on the Github repository with each roadmap item listed as a task. Once
all tasks are complete, the version will be released and the next started.

Once an item has been implemented it will be removed from the roadmap. If you
would like to see a history of changes on the existing codebase, check out the
Changelog (CHANGELOG.md in the project root) to see what was included in each
version of the library. In most cases, roadmap items will be removed from this
document and placed there upon completion.

Note that the timelines associated with each change are vague at best. The
project team is not currently big enough to realistically make any promises, so
timing is often left out to prevent folks from feeling cheated if something
takes longer than expected.


## A Note about Github issues and projects
A fair question to ask is why the roadmap is not being managed within the issue
and project features of Github itself, since this is where the project is
currently hosted. Indeed, suggestions submitted by the community are tracked as
issues, and projects are already created for ongoing work. There are a few
reasons that a separate roadmap is maintained:
 * **Issues are used to exclusively track bugs and community requests.**
   This certainly isn't a hard and fast rule, and isn't followed by many other
   projects, but it is how Ruby Dragon is managed. Keeping the issue count as a
   clear indicator of known problems and community requests lets the project
   maintainers (and anyone interested in looking at how well it is being
   maintained) immediately see how much outstanding work exists. Of course,
   the roadmap may have features requested by the community or enhancements made
   clear by bug reports, but it will also have a number of features and tweaks
   that have a lower priority.
 * **Project direction should come packaged with the product.**
   Again this isn't a commonly followed rule, but it is one that the project
   author follows. Anyone that obtains the source code of the project at a
   single point in time should be able to quickly see the current direction of
   the project. Maintaining the roadmap within the version control of the source
   itself facilitates this, the same way that licensing and copyright
   notifications are traditionally bundled with code. And if you don't care,
   you can always ignore them.

