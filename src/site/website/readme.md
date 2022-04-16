## Introduction

The *docsy* theme was customized to support specific needs of the tangly OS documentation static website.

## Footer

The footer template was adapted to support the specific needs of the tangly OS documentation static website.
The open source copyright of our site is not well displayed in the standard footer template

- docsy/layouts/partials/footer.html

## Attachments

For the attachment feature we added:

- docsy/assets/scss/shortcodes.scss
- docsy/assets/scss/main.scss _added import of the shortcodes.scss styling
- docsy/layouts/shortcodes/attachments.html

## Comments

For the comment feature we added:

- docsy/layouts/blog/content.html _added utterances code for comments_
- docsy/layouts/partials/comments-utterances.html _partial for comment template_

## Asciidoc Stylesheet

Exploration was started to improve the styling of [asciidoc](https://asciidoc.org) documents:

- docsy/assets/scss/_styles_project.scss
