# Copyright (c) 2004-2018 Adam Karpierz
# Licensed under proprietary License
# Please refer to the accompanying LICENSE file.

from __future__ import absolute_import, print_function

import unittest
import sys
import os
import logging

from . import test_dir

test_java = os.path.join(test_dir, "java")


def test_suite(names=None, omit=()):

    from .python import __name__ as pkg_name
    from .python import __path__ as pkg_path
    import unittest
    import pkgutil
    if names is None:
        names = [name for _, name, _ in pkgutil.iter_modules(pkg_path)
                 if name != "__main__" and name not in omit]
    names = [".".join((pkg_name, name)) for name in names]
    tests = unittest.defaultTestLoader.loadTestsFromNames(names)
    return tests


class UnknownError(RuntimeError):
    """ """

class ThreadNotAttachedError(RuntimeError):
    """ """

class VersionNotSupportedError(RuntimeError):
    """ """
class NotEnoughMemoryError(RuntimeError):
    """ """

class JVMAlreadyExistError(RuntimeError):
    """ """

class InvalidArgumentError(RuntimeError):
    """ """


def main():

    import jt.jvm.platform
    jvm_path = jt.jvm.platform.JVMFinder().get_jvm_path()

    print("Running testsuite using JVM:", jvm_path, "\n", file=sys.stderr)

    package = sys.modules[__package__]
    package.jvm = jvm = jt.jvm.JVM(jvm_path)
    package.UnknownError             = UnknownError
    package.ThreadNotAttachedError   = ThreadNotAttachedError
    package.VersionNotSupportedError = VersionNotSupportedError
    package.NotEnoughMemoryError     = NotEnoughMemoryError
    package.JVMAlreadyExistError     = JVMAlreadyExistError
    package.InvalidArgumentError     = InvalidArgumentError
    jvm.ExceptionsMap = {
        jt.jvm.EStatusCode.ERR:       UnknownError,
        jt.jvm.EStatusCode.EDETACHED: ThreadNotAttachedError,
        jt.jvm.EStatusCode.EVERSION:  VersionNotSupportedError,
        jt.jvm.EStatusCode.ENOMEM:    NotEnoughMemoryError,
        jt.jvm.EStatusCode.EEXIST:    JVMAlreadyExistError,
        jt.jvm.EStatusCode.EINVAL:    InvalidArgumentError,
    }
    jvm.start("-Djava.class.path={}".format(
              os.pathsep.join([os.path.join(test_java, "classes")])),
              "-ea", "-Xms16M", "-Xmx512M")
    try:
        tests = test_suite(sys.argv[1:] or None)
        result = unittest.TextTestRunner(verbosity=2).run(tests)
    finally:
        jvm.shutdown()

    sys.exit(0 if result.wasSuccessful() else 1)


if __name__.rpartition(".")[-1] == "__main__":
    # logging.basicConfig(level=logging.INFO)
    # logging.basicConfig(level=logging.DEBUG)
    main()