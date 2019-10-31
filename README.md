# NaNoWriMo Tracker

A small program that tracks your writing progress and can update your [NaNoWriMo](https://nanowrimo.org/) wordcount while you write.

## CLI Mode

If you want the program to run in the terminal, you can add the parameter `--nogui`.
It will still monitor, save, and upload your progress, but will do so silently apart from that.

### Supported Filetypes
 - Text Files (TXT, RTF)
 - Word Documents (ODT, DOC, DOCX)
 - Scrivener Projects (SCRIV)
 - More upon request

### Features
 - ~~Updates your wordcount on the [NaNoWriMo website](https://nanowrimo.org/)~~ (API has changed, will fix as soon as I have access to the new API)
 - Keeps track of your writing progress
 - Draws a pretty graph of your progress
 - Autodetects your document type
 - Specify your own custom wordgoals
 
### Planned Features
 - Private Servers, so you can use the tool also outside of [NaNoWriMo](https://nanowrimo.org/)

Special Thanks to [Kevin Alberts](https://github.com/Kurocon) for the implementation of the NaNoWriMo-API.

# License

This project uses The unlicense.
This essentially means you are free to do anything with it.
It is, however, highly appreciated if you make your version of the program publicly available.
