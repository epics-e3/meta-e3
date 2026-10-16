#!/usr/bin/env python3
"""Map changed files of meta-e3 to the recipe names (PN) they affect.

Must run inside "kas shell .ci/kas/base.yml". Bitbake parses the layer, so
recipes that set PN explicitly are reported correctly.

Usage: changed-recipes.py <file relative to the layer root>...
       changed-recipes.py --all
Prints the affected recipe names on one line, space separated.
"""
import os
import sys

import bb.tinfoil

LAYER = os.path.realpath(os.path.join(os.path.dirname(__file__), "..", ".."))
# Changes here can affect every recipe in the layer.
GLOBAL_DIRS = ("classes/", "conf/", ".ci/", ".github/")


def main(files):
    with bb.tinfoil.Tinfoil() as tinfoil:
        tinfoil.prepare(quiet=2)
        pkg_fn = tinfoil.cooker.recipecaches[""].pkg_fn
        # Real recipe files of this layer (skip virtual:native etc. variants).
        # Bitbake paths are not normalized (e.g. /work/build/../../repo/...).
        recipes = {os.path.realpath(fn): pn for fn, pn in pkg_fn.items()
                   if not fn.startswith("virtual:")
                   and os.path.realpath(fn).startswith(LAYER + os.sep)}

        selected = set()
        if files == ["--all"]:
            selected.update(recipes.values())
            files = []
        for rel in files:
            path = os.path.join(LAYER, rel)
            if rel.startswith(GLOBAL_DIRS):
                selected.update(recipes.values())
            elif rel.endswith(".bb"):
                selected.update(pn for fn, pn in recipes.items() if fn == path)
            elif rel.endswith(".bbappend"):
                for fn, pn in pkg_fn.items():
                    appends = tinfoil.get_file_appends(fn)
                    if path in (os.path.realpath(a) for a in appends):
                        selected.add(pn)
            else:
                # .inc, patches and other files: recipes in the same directory
                # or a parent directory of the file.
                selected.update(pn for fn, pn in recipes.items()
                                if path.startswith(os.path.dirname(fn) + os.sep))

    print(" ".join(sorted(selected)))


if __name__ == "__main__":
    main(sys.argv[1:])
