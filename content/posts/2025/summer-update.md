---
title: EDN-infused plain html forms
description: And some free t-shirt ideas
slug: edn-html-forms
image: https://biffweb.com/cards/edn-html-forms.png
published: 2025-06-20T12:36:00 AM
---

Merry solstice. After about a year, I'm roughly 80% done with the
[Yakread](https://github.com/jacobobryant/yakread) rewrite. Now all that's left is the remaining
80%. [My last post](https://biffweb.com/p/structuring-large-codebases/) is still a good explanation
of the new Biff things I've been hacking on as part of that. Over the past couple weeks I've also
been thinking about how to do forms.

So far Biff hasn't provided anything special for forms: if you need an email address, you do
`[:input {:name "email"} ...]`, you get the value as a string from `(-> request :params :email)`,
you parse it if needed (not needed in this case), then you stick it in a map like `{:user/email
email, ...}` or whatever. Works fine for small forms; no need to over-complicate things.

But what if you have form with 50 fields? It would be nice if we could get EDN from the frontend,
e.g. `{:user/email "abc@example.com", :user/age 666}` instead of `{:email "abc@example.com", :age
"666"}`. Same as you get if you're doing a cljs frontend instead of htmx. htmx users deserve nice
things too!

I've started rendering my form fields like `[:input {:name (pr-str :user/email)} ...]` (turns out
`:name` will accept just about anything) and then using a
[wrap-parse-form](https://github.com/jacobobryant/yakread/blob/9052fe12b7df9bdb944d6998e37432905b1ec229/src/com/yakread/lib/form.clj#L54)
middleware to parse the requests. That function attempts to parse each key in the form params with
`edn/read-string` ([fast-edn](https://github.com/tonsky/fast-edn), actually), skipping keys that
fail. For each parsed key, we then check your Biff app's Malli schema to see if that key is defined
and what its type is. We use the type to figure out how to parse the form value. There are default
parse functions for a few common types (`int` is `Long/parseLong`, `:uuid` is `parse-uuid`, etc).
For other types, you can define a custom form parser in your schema, for example:

```clojure
(def schema
  {::cents [:int {:biff.form/parser
                  #(-> %
                       (Float/parseFloat)
                       (* 100)
                       (Math/Round))}]
   :ad [:map {:closed true}
        [:ad/budget ::cents]
        ...
```

Now if I have a form field like `[:input {:name (pr-str :ad/budget)} ...]` and the user types in
`12.34`, on the backend I'll get `{:ad/budget 1234, ...}` automagically.

The form data isn't quite self-describing like EDN is: it relies on schema defined somewhere outside
the form. I started out doing stuff like `[:input {:name (pr-str {:field :user/favorite-number,
:type :int})} ...]` (seriously, you really can put anything in `:name`), but since I'm writing this
middleware for Biff apps specifically, I didn't feel like that approach was adding much value. And
I'm all about value.

What about forms with multiple entities? If your `:name` value is a vector like `(pr-str [:user
:user/email])`, then `wrap-parse-form` will do an `(assoc-in params [:user :user/email] ...)`. I
don't at the moment have any special support for arrays of things, but you can do `:name (pr-str
[:users 3 :user/email])` and then you'll get `{:users {3 {:user/email ...}}}` in the request.

---

**Other Biff news**

Remaining things in the Yakread TODO list include finishing the ad system, adding premium plans,
precomputing some recommendation models so that page loads are faster, and setting up email digests
of your subscriptions. How long could that take? Surely not long! Oh, and then I just need to
migrate all the users over from the [currently-in-production](https://yakread.com) Yakread as well
as [another similar app](https://thesample.ai) that stopped being profitable last year... but yes,
certainly not long.

Once that's humming along and my monthly side project operational costs are back in the double
digits, it'll be time for a much needed Biff release. I'll extract some of the stuff from Yakread
and package it up real nice, and then go through some [
maintenance](https://github.com/jacobobryant/biff/issues/217) tasks that have been... festering,
shall we say. And _then_ it's time for...

**xᴛᴅʙ ᴠᴇʀsɪᴏɴ 2**: at last. Everyone's favorite 4-letter immutable database is [out of
beta](https://xtdb.com/blog/launching-xtdb-v2). Which means it's really time to get Biff on it. I
figure Yakread, once the rewrite is done, will make a nice open-source example of porting a
nontrivial app from XTDB v1 to v2. So expect a big Biff release with migration guide and all that.
Hopefully by the end of the year 😬. Maybe I could even look into integrating XTDB with Rama.

Until we meet again, perhaps at the equinox. Or at the conj. I've got my ticket already.

Two free t-shirt ideas:

- "(got? :lisp)" -- styled to look like those "got milk?" fridge magnets.
- "Breaking changes are for the weak" -- not sure how to style it, but this t-shirt definitely needs
  to exist.
