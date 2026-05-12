/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.core.style.ToStringCreator;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @ModifiedBy Tanmay Ghosh
 * @Modified By Vivek Bengre
 */
@Entity
@Table(name = "owners")
public class CyValPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @NotFound(action = NotFoundAction.IGNORE)
    private int id;

    @Column(name = "title")
    @NotFound(action = NotFoundAction.IGNORE)
    private String title;

    @Column(name = "body")
    @NotFound(action = NotFoundAction.IGNORE)
    private String body;

    @Column(name = "mediaType")
    @NotFound(action = NotFoundAction.IGNORE)
    private String mediaType;

    @Column(name = "author")
    @NotFound(action = NotFoundAction.IGNORE)
    private String author;

    @Column(name = "visibility")
    @NotFound(action = NotFoundAction.IGNORE)
    private String visibility;

    public CyValPost(){
        
    }

    public CyValPost(String title, String body, String mediaType,
                     String author, String visibility){
        this.title = title;
        this.body = body;
        this.mediaType = mediaType;
        this.author = author;
        this.visibility = visibility;
    }

        public int getId() { return id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }

        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }

    @Override
    public String toString() {
        return "CyValPost{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", author='" + author + '\'' +
                ", visibility='" + visibility + '\'' +
                '}';
    }
}
