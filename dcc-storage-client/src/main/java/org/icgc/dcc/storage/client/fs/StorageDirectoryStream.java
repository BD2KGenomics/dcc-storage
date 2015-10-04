/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.storage.client.fs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.icgc.dcc.storage.client.metadata.Entity;

import lombok.SneakyThrows;
import lombok.val;

public class StorageDirectoryStream implements DirectoryStream<Path> {

  private StoragePath path;
  private Filter<? super Path> filter;

  public StorageDirectoryStream(StoragePath path, Filter<? super Path> filter) {
    this.path = path;
    this.filter = filter;
  }

  @Override
  public Iterator<Path> iterator() {
    val entities = getEntities();
    if (path.getNameCount() == 0) {
      return entities.stream().map(entity -> entity.getGnosId()).distinct().map(this::gnosIdPath)
          .filter(this::filterPath).iterator();
    } else {
      val gnosId = path.getParts()[0];
      return entities.stream().filter(entity -> entity.getGnosId().equals(gnosId)).map(this::entityPath)
          .filter(this::filterPath).iterator();
    }
  }

  private List<Entity> getEntities() {
    val fileSystem = path.getFileSystem();
    return fileSystem.getProvider().getFileService().getEntities();
  }

  private Path gnosIdPath(String gnosId) {
    return new StoragePath(
        path.getFileSystem(),
        new String[] { gnosId },
        true);
  }

  private Path entityPath(Entity entity) {
    return new StoragePath(
        path.getFileSystem(),
        new String[] { entity.getGnosId(), entity.getFileName() },
        true);
  }

  @SneakyThrows
  private boolean filterPath(Path path) {
    return filter.accept(path);
  }

  @Override
  public void close() throws IOException {
  }

}