# sbt-calver

`sbt-calver` is an sbt plugin that automatically sets version of your project to [calver](https://calver.org/) format.

Inpired by:

* [`sbt-dynver`](https://github.com/sbt/sbt-dynver/tree/main)

## Setup

Add this to your sbt build plugins:

```sbt
addSbtPlugin("me.slivkamiro.sbt" % "sbt-calver" % "x.y.z")
```

Supported configuration options:

* `versionFormat` - changes how the date is formatted in version , defaults to `YY.0M` which produces version like `YY.0M.patch`

Assuming default `versionFormat` your version will be set to:

| Current Date | Tag          | Dist  | HEAD SHA | dirty | version | description |
|    :---:     |  :---:       | :---: |   :---:  | :---: |  :---:  | :--- |
| 2023-07-11   | &lt;none&gt; | -     | 0123abc  | no    | 23.07.1-0123abc | untagged repository |
| 2023-07-11   | &lt;none&gt; | -     | 0123abc  | yes    | 23.07.1-0123abc-20230711-1030 | uncomitted changes on untagged repository |
| 2023-07-11   | 23.06.1      | 0     | -        | no    | 23.07.1 | at the tag at different date - no code changes needed to change version |
| 2023-07-11   | 23.06.1      | -     | 0123abc  | yes    | 23.07.1-0123abc-20230811-1030 | uncomitted changes at different date |
| 2023-07-11   | 23.06.1      | 1     | 0123abc  | no    | 23.07.1-0123abc | ahead of master at different date |
| 2023-07-11   | 23.07.1      | 0     | -        | no    | 23.07.1 | at the tag |
| 2023-07-11   | 23.07.1      | -     | 0123abc  | yes    | 23.07.2-0123abc-20230711-1030 | uncomitted changes |
| 2023-07-11   | 23.07.1      | 1     | 0123abc  | no    | 23.07.2-0123abc | ahead of master |

Where:

* `tag` means what is the latest tag (relative to HEAD)
* `dist` means the distance of the HEAD commit from the tag
* `dirty` refers to whether there are local changes in the git repo
