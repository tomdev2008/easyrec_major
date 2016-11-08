/*
 * Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
 *
 * This file is part of easyrec.
 *
 * easyrec is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * easyrec is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.easyrec.utils.io;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Stephan
 */
public class TreeCopy implements FileVisitor<Path> {
    
    // logging
    private final Log logger = LogFactory.getLog(this.getClass());
    
    private final Path source;
    private final Path target;

    public TreeCopy(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes bfa) throws IOException {
        // before visiting entries in a directory we copy the directory
        // (okay if directory already exists).
        CopyOption[] options =  new CopyOption[] { REPLACE_EXISTING };
        Path srcDir = source.relativize(dir);
        Path newdir = resolve(target,srcDir);
        try {
            Files.copy(dir, newdir, options);
        } catch (FileAlreadyExistsException x) {
            // ignore
        } catch (IOException x) {
            logger.error("Unable to create: " + newdir, x);
            return SKIP_SUBTREE;
        }
        return CONTINUE;

    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes bfa) throws IOException {
        
        Path src = source.relativize(file);
        Path tar = resolve(target, src);
        Files.copy(file, tar);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ioe) throws IOException {
        if (ioe instanceof FileSystemLoopException) {
            logger.error("cycle detected: " + file);
        } else {
            logger.error("Unable to copy: " + file, ioe);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException ioe) throws IOException {
        if (ioe != null) {
            logger.error(ioe);
        }
        return CONTINUE;
    }
    
        /**
     * Copy source file to target location. If {@code prompt} is true then
     * prompt user to overwrite target if it exists. The {@code preserve}
     * parameter determines if file attributes should be copied/preserved.
     */
//    private void copyFile(Path source, Path target, boolean preserve) {
//        CopyOption[] options = (preserve) ?
//            new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING } :
//            new CopyOption[] { REPLACE_EXISTING };
//            try {
//                Files.copy(source, target, options);
//            } catch (IOException x) {
//                logger.error("Unable to copy: " + source, x);
//            }
//    }
    
     /**
     * Resolve a path against another path with a potentially different
     * {@link FileSystem} or {@link FileSystemProvider}
     *
     * <p>{@link Path#resolve(Path)} will refuse to operate if its argument is
     * issued from a different provider (with a {@link
     * ProviderMismatchException}); moreover, if the argument is issued from the
     * same provider but is on a different filesystem, the result of the
     * resolution may be on the argument's filesystem, not the caller's.</p>
     *
     * <p>This method will attempt to resolve the second path against the first
     * so that the result is <em>always</em> associated to the filesystem (and
     * therefore provider) of the first argument. For the resolution to operate,
     * the following conditions must be met for {@code path2}:</p>
     *
     * <ul>
     *     <li>if it is not absolute, it must not have a root;</li>
     *     <li>if it is absolute, it must have a root, and the string
     *     representation of this root must match a string representation of one
     *     possible root of the first path's filesystem.</li>
     * </ul>
     *
     * <p>If the conditions above are not satisfied, this method throws an
     * {@link UnresolvablePathException} (unchecked).</p>
     *
     * <p>If both paths are issued from the same filesystem, this method will
     * delegate to {@code path1}'s {@code .resolve()}; if they are from
     * different filesystems but share the same provider, this method returns:
     * </p>
     *
     * <pre>
     *     path1.resolve(path1.getFileSystem().getPath(path2.toString()))
     * </pre>
     *
     * <p>This means that for instance it is possible to resolve a Unix path
     * against a Windows path, or the reverse, as long as the second path is
     * not absolute (the root paths of both filesystems are incompatible):</p>
     *
     * <ul>
     *     <li>resolving {@code foo/bar/baz} against {@code c:} will return
     *     {@code c:\foo\bar\baz};</li>
     *     <li>resolving {@code baz\quux} against {@code /foo/bar} will return
     *     {@code /foo/bar/baz/quux}.</li>
     * </ul>
     *
     * @param path1 the first path
     * @param path2 the second path
     * @return the resolved path
     * @throws UnresolvablePathException see description
     * @throws InvalidPathException {@code path2} is from a different provider,
     * and one of its name elements is invalid according to {@code path1}'s
     * filesystem
     *
     * @see FileSystem#getPath(String, String...)
     * @see FileSystem#getRootDirectories()
     */
    @SuppressWarnings("ObjectEquality")
    private Path resolve(final Path path1, final Path path2) throws IOException
    {

        final FileSystem fs1
            = Objects.requireNonNull(path1).getFileSystem();
        final FileSystem fs2
            = Objects.requireNonNull(path2).getFileSystem();

        if (fs1 == fs2)
            return path1.resolve(path2);

        if (fs1.provider() == fs2.provider())
            return path1.resolve(fs1.getPath(path2.toString()));

        final boolean isAbsolute = path2.isAbsolute();
        final Path root2 = path2.getRoot();

        final String errmsg = isAbsolute
            ? "path to resolve is absolute but has no root"
            : "path to resolve is not absolute but has a root";

        // Always tricky to read an xor...
        if (isAbsolute ^ root2 != null)
            throw new IOException(errmsg);

        Path ret;

        if (isAbsolute) {
            /*
             * Check if the root of path2 is compatible with path1
             */
            final String path2Root = root2.toString();

            boolean foundRoot = false;

            for (final Path root1: fs1.getRootDirectories())
                if (root1.toString().equals(path2Root))
                    foundRoot = true;

            if (!foundRoot)
                throw new IOException("root of path to resolve "
                    + "is incompatible with source path");

            ret = fs1.getPath(path2Root);
        } else {
            /*
             * Since the empty path is defined as having one empty name
             * component, which is rather awkward, we don't want to take the
             * risk of triggering bugs in FileSystem#getPath(); instead, check
             * that the string representation of path2 is empty, and if it is,
             * just return path1.
             */
            if (path2.toString().isEmpty())
                return path1;

            ret = path1;
        }

        for (final Path element: path2)
            ret = ret.resolve(element.toString());

        return ret;
    }

 
}
